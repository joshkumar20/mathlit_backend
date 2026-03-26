package com.mathlit.backend.controller;

import com.mathlit.backend.dto.LeaderboardResponse;
import com.mathlit.backend.service.LeaderboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/weekly")
    public LeaderboardResponse getWeekly(HttpServletRequest request,
                                          @RequestParam(defaultValue = "50") int limit) {
        String uid = (String) request.getAttribute("uid");
        return leaderboardService.getWeeklyLeaderboard(uid, limit);
    }

    @GetMapping("/alltime")
    public LeaderboardResponse getAllTime(HttpServletRequest request,
                                          @RequestParam(defaultValue = "50") int limit) {
        String uid = (String) request.getAttribute("uid");
        return leaderboardService.getAllTimeLeaderboard(uid, limit);
    }

    @GetMapping("/daily")
    public LeaderboardResponse getDaily(HttpServletRequest request) {
        String uid = (String) request.getAttribute("uid");
        return leaderboardService.getDailyLeaderboard(uid);
    }
}
