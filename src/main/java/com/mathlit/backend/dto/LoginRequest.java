package com.mathlit.backend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String displayName;
    private String avatarUrl;
}
