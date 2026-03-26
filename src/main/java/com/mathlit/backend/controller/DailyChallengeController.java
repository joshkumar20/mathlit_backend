package com.mathlit.backend.controller;

import com.mathlit.backend.dto.DailyChallengeDto;
import com.mathlit.backend.dto.DailyChallengeResultDto;
import com.mathlit.backend.dto.DailyChallengeSubmitDto;
import com.mathlit.backend.service.DailyChallengeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/daily")
public class DailyChallengeController {

    private final DailyChallengeService dailyChallengeService;

    public DailyChallengeController(DailyChallengeService dailyChallengeService) {
        this.dailyChallengeService = dailyChallengeService;
    }

    @GetMapping("/today")
    public DailyChallengeDto getToday(HttpServletRequest request) {
        String uid = (String) request.getAttribute("uid");
        return dailyChallengeService.getTodayChallenge(uid);
    }

    @PostMapping("/submit")
    public DailyChallengeResultDto submit(HttpServletRequest request,
                                           @RequestBody DailyChallengeSubmitDto dto) {
        String uid = (String) request.getAttribute("uid");
        return dailyChallengeService.submitChallenge(uid, dto);
    }

    @GetMapping("/status")
    public Map<String, Boolean> getStatus(HttpServletRequest request) {
        String uid = (String) request.getAttribute("uid");
        return Map.of("completed", dailyChallengeService.hasCompletedToday(uid));
    }
}
