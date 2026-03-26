package com.mathlit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntryDto {
    private long rank;
    private String uid;
    private String displayName;
    private String avatarUrl;
    private int level;
    private long score;
}
