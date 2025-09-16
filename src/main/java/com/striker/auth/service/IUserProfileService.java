package com.striker.auth.service;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.UserProfileDto;
import com.striker.auth.entity.UserProfile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("userProfileService")
public interface IUserProfileService {
    ApiResponse getUserProfile(UUID userId);

    ApiResponse updateUserProfile(UserProfileDto userProfileDto);

    ApiResponse deleteUserProfile(UUID userId);

    ApiResponse addUserProfile(UserProfileDto userProfile);
}
