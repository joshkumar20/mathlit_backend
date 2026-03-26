package com.mathlit.backend.controller;

import com.mathlit.backend.dto.StreakStatusDto;
import com.mathlit.backend.model.User;
import com.mathlit.backend.repository.UserRepository;
import com.mathlit.backend.service.StreakService;
import com.mathlit.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/streak")
public class StreakController {

    private final StreakService streakService;
    private final UserService userService;
    private final UserRepository userRepository;

    public StreakController(StreakService streakService, UserService userService, UserRepository userRepository) {
        this.streakService = streakService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/ping")
    public StreakStatusDto ping(HttpServletRequest request) {
        String uid = (String) request.getAttribute("uid");
        User user = userService.findByUid(uid);
        StreakStatusDto result = streakService.updateStreak(user);
        userRepository.save(user);
        return result;
    }

    @GetMapping("/status")
    public StreakStatusDto getStatus(HttpServletRequest request) {
        String uid = (String) request.getAttribute("uid");
        User user = userService.findByUid(uid);
        return new StreakStatusDto(
                user.getCurrentStreak(),
                user.getLongestStreak(),
                user.getLastPlayedDate(),
                false,
                false
        );
    }
}
