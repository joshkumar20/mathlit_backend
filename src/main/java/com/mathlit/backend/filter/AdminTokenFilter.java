package com.mathlit.backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protects /admin/api/** routes (except /admin/api/auth/login).
 * Tokens are generated on login, stored in memory, expire after 24 h.
 */
@Component
public class AdminTokenFilter extends OncePerRequestFilter {

    private static final long EXPIRY_MS = 24 * 60 * 60 * 1000L;
    private static final ConcurrentHashMap<String, Long> tokens = new ConcurrentHashMap<>();

    // ── Token management (called by AdminApiController) ───────────────────────

    public static String generateToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, System.currentTimeMillis() + EXPIRY_MS);
        return token;
    }

    public static boolean isValid(String token) {
        if (token == null) return false;
        Long expiry = tokens.get(token);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    public static void revoke(String token) {
        if (token != null) tokens.remove(token);
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip for non-admin-API paths and for the login endpoint itself
        return !path.startsWith("/admin/api/") || path.equals("/admin/api/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        if (!isValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized — invalid or expired admin token\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);
        return request.getHeader("X-Admin-Token");
    }
}