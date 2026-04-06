package com.mathlit.backend.service;

import com.mathlit.backend.dto.CreateNotificationRequest;
import com.mathlit.backend.dto.NotificationDto;
import com.mathlit.backend.model.Notification;
import com.mathlit.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public List<NotificationDto> getAll() {
        return repo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public NotificationDto create(CreateNotificationRequest req) {
        Notification n = new Notification();
        n.setTitle(req.getTitle());
        n.setBody(req.getBody());
        n.setType(req.getType() != null ? req.getType().toUpperCase() : "INFO");
        return toDto(repo.save(n));
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setBody(n.getBody());
        dto.setType(n.getType());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}