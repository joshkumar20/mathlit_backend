package com.mathlit.backend.service;

import com.mathlit.backend.dto.LoginRequest;
import com.mathlit.backend.dto.UpdateProfileRequest;
import com.mathlit.backend.dto.UserProfileDto;
import com.mathlit.backend.model.User;
import com.mathlit.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserProfileDto loginOrRegister(String uid, String email, LoginRequest request) {
        boolean isNew = userRepository.findByFirebaseUid(uid).isEmpty();

        User user = userRepository.findByFirebaseUid(uid).orElseGet(() -> {
            User u = new User();
            u.setFirebaseUid(uid);
            u.setEmail(email);
            return u;
        });

        if (request.getDisplayName() != null) user.setDisplayName(request.getDisplayName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (user.getEmail() == null) user.setEmail(email);

        User saved = userRepository.save(user);
        UserProfileDto dto = toDto(saved);
        dto.setNewUser(isNew);
        return dto;
    }

    public UserProfileDto getProfile(String uid) {
        User user = findByUid(uid);
        return toDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(String uid, UpdateProfileRequest request) {
        User user = findByUid(uid);
        if (request.getDisplayName() != null) user.setDisplayName(request.getDisplayName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        return toDto(userRepository.save(user));
    }

    public User findByUid(String uid) {
        return userRepository.findByFirebaseUid(uid)
                .orElseThrow(() -> new RuntimeException("User not found: " + uid));
    }

    public UserProfileDto toDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setUid(user.getFirebaseUid());
        dto.setDisplayName(user.getDisplayName());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setXp(user.getXp());
        dto.setLevel(user.getLevel());
        dto.setCurrentStreak(user.getCurrentStreak());
        dto.setLongestStreak(user.getLongestStreak());
        dto.setTotalGames(user.getTotalGames());
        dto.setTotalScore(user.getTotalScore());
        dto.setTotalCorrect(user.getTotalCorrect());
        dto.setTotalAttempted(user.getTotalAttempted());
        dto.setHighestScore(user.getHighestScore());
        if (user.getTotalAttempted() > 0) {
            dto.setAccuracy((double) user.getTotalCorrect() / user.getTotalAttempted() * 100);
        }
        return dto;
    }
}
