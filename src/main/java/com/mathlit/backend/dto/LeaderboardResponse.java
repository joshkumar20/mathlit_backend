package com.mathlit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class LeaderboardResponse {
    private LocalDateTime updatedAt;
    private List<LeaderboardEntryDto> entries;
    private MyRankDto myRank;
}
