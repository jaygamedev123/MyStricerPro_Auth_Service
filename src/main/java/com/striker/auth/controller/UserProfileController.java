package com.striker.auth.controller;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.SocialLoginRequestDto;
import com.striker.auth.dto.UserProfileDto;
import com.striker.auth.service.IUserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/userprofiles")
public class UserProfileController {

    private final IUserProfileService userProfileService;

    public UserProfileController(IUserProfileService userProfileService) {
        log.info("UserProfileController initialized");
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUserProfile(@RequestParam UUID userId) {
        log.info("Received request to get user profile for userId: {}", userId);
        ApiResponse response = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ApiResponse> updateUserProfile(@RequestBody UserProfileDto userProfileDto) {
        log.info("Received request to update user profile: {}", userProfileDto);
        ApiResponse response = userProfileService.updateUserProfile(userProfileDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> softDeletionOfUserProfile(@RequestParam UUID userId) {
        log.info("Received request to delete user profile for userId: {}", userId);
        ApiResponse response = userProfileService.deleteUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addUserProfile(@RequestBody UserProfileDto userProfileDto) {
        log.info("Received request to add user profile: {}", userProfileDto);
        ApiResponse response = userProfileService.addUserProfile(userProfileDto);
        return ResponseEntity.ok(response);
    }

    // get data from frontend
    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse> socialLogin(@RequestBody SocialLoginRequestDto request) {
        log.info("Received social login request for provider: {} and providerUserId: {}", request.provider(), request.providerUserId());
        ApiResponse response = userProfileService.handleSocialLogin(request);
        return ResponseEntity.ok(response);
    }
}
