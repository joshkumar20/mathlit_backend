package com.mathlit.backend.controller;

import com.mathlit.backend.dto.CreateNotificationRequest;
import com.mathlit.backend.dto.NotificationDto;
import com.mathlit.backend.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private static final String ADMIN_SECRET = "mathlit-admin-2024";

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    /** All authenticated users can fetch notifications. */
    @GetMapping("/notifications")
    public List<NotificationDto> getAll(HttpServletRequest request) {
        // uid attribute is set by FirebaseAuthFilter — ensures user is authenticated
        String uid = (String) request.getAttribute("uid");
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return service.getAll();
    }

    /** Admin-only: create a new notification (protected by a shared secret header). */
    @PostMapping("/admin/notifications")
    public NotificationDto create(HttpServletRequest request,
                                  @RequestBody CreateNotificationRequest body) {
        String secret = request.getHeader("X-Admin-Secret");
        if (!ADMIN_SECRET.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return service.create(body);
    }
}
