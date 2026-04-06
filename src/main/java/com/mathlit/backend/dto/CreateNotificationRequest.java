package com.mathlit.backend.dto;

import lombok.Data;

@Data
public class CreateNotificationRequest {
    private String title;
    private String body;
    private String type; // INFO, UPDATE, ACHIEVEMENT, PROMO (defaults to INFO if null)
}
