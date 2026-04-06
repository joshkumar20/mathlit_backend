package com.mathlit.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private String title;
    private String body;
    private String type;
    private LocalDateTime createdAt;
}
