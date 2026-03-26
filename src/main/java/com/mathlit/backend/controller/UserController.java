package com.mathlit.backend.controller;

import com.mathlit.backend.dto.LoginRequest;
import com.mathlit.backend.dto.UpdateProfileRequest;
import com.mathlit.backend.dto.UserProfileDto;
import com.mathlit.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    public UserProfileDto login(HttpServletRequest request, @RequestBody LoginRequest body) {
        String uid = (String) request.getAttribute("uid");
        String email = (String) request.getAttribute("email");
        return userService.loginOrRegister(uid, email, body);
    }

    @GetMapping("/user/profile")
    public UserProfileDto getProfile(HttpServletRequest request) {
        String uid = (String) request.getAttribute("uid");
        return userService.getProfile(uid);
    }

    @PutMapping("/user/profile")
    public UserProfileDto updateProfile(HttpServletRequest request, @RequestBody UpdateProfileRequest body) {
        String uid = (String) request.getAttribute("uid");
        return userService.updateProfile(uid, body);
    }

    @GetMapping("/user/stats")
    public UserProfileDto getStats(HttpServletRequest request) {
        String uid = (String) request.getAttribute("uid");
        return userService.getProfile(uid);
    }
}
