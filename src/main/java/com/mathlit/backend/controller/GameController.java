package com.mathlit.backend.controller;

import com.mathlit.backend.dto.GameSessionDto;
import com.mathlit.backend.dto.GameSessionResponse;
import com.mathlit.backend.service.GameSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/game")
public class GameController {

    private final GameSessionService gameSessionService;

    public GameController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @PostMapping("/session")
    public GameSessionResponse saveSession(HttpServletRequest request, @RequestBody GameSessionDto dto) {
        String uid = (String) request.getAttribute("uid");
        return gameSessionService.saveSession(uid, dto);
    }

    @GetMapping("/sessions")
    public List<GameSessionDto> getSessions(HttpServletRequest request,
                                             @RequestParam(defaultValue = "10") int limit) {
        String uid = (String) request.getAttribute("uid");
        return gameSessionService.getRecentSessions(uid, limit);
    }
}
