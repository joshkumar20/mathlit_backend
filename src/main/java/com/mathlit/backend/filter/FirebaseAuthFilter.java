package com.mathlit.backend.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private void setAuthentication(HttpServletRequest request, String uid, String email) {
        request.setAttribute("uid", uid);
        request.setAttribute("email", email);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(401, "Missing or invalid Authorization header");
            return;
        }
        String token = header.substring(7);

        // DEV TEST MODE: token = "test-<uid>" bypasses Firebase verification
        if (token.startsWith("test-")) {
            String testUid = token.substring(5);
            setAuthentication(request, testUid, testUid + "@test.com");
            chain.doFilter(request, response);
            return;
        }

        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
            setAuthentication(request, decoded.getUid(), decoded.getEmail());
            chain.doFilter(request, response);
        } catch (FirebaseAuthException e) {
            response.sendError(401, "Invalid or expired Firebase token");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip for health check and all admin routes (handled by AdminTokenFilter)
        return path.equals("/health") || path.startsWith("/admin/");
    }
}
