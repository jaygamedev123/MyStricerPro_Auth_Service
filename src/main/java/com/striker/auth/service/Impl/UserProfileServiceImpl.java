package com.striker.auth.service.Impl;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.SocialLoginRequestDto;
import com.striker.auth.dto.UserProfileDto;
import com.striker.auth.entity.UserProfile;
import com.striker.auth.entity.UserProvider;
import com.striker.auth.repos.IUserProfileRepo;
import com.striker.auth.repos.IUserProviderRepo;
import com.striker.auth.service.IUserProfileService;
import com.striker.auth.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service("userProfileService")
public class UserProfileServiceImpl implements IUserProfileService {

    private final IUserProfileRepo userProfileRepo;
    private final IUserProviderRepo userProviderRepo;
    private final JwtService jwtService;

    public UserProfileServiceImpl(IUserProfileRepo userProfileRepo,
                                  IUserProviderRepo userProviderRepo,
                                  JwtService jwtService) {
        log.debug("UserProfileServiceImpl initialized");
        this.userProfileRepo = userProfileRepo;
        this.userProviderRepo = userProviderRepo;
        this.jwtService = jwtService;
    }

    @Override
    public ApiResponse getUserProfile(UUID userId) {
        log.info("Fetching user profile for userId: {}", userId);
        try {
            Optional<UserProfile> userProfileOpt = userProfileRepo.findById(userId);
            if (userProfileOpt.isPresent()) {
                return ApiResponse.builder()
                        .httpStatus(HttpStatus.OK)
                        .success(true)
                        .message("User profile fetched successfully")
                        .data(userProfileOpt.get())
                        .build();
            }
            return ApiResponse.builder()
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .success(false)
                    .message("User profile not found")
                    .build();
        } catch (Exception e) {
            log.error("Error fetching user profile for userId: {}", userId, e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching user profile");
        }
    }

    @Override
    public ApiResponse updateUserProfile(UserProfileDto userProfileDto) {
        log.info("Updating user profile for userId: {}", userProfileDto.getUserId());
        if (userProfileDto.getUserId() == null) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "userId is required for update");
        }
        try {
            Optional<UserProfile> existingProfileOpt = userProfileRepo.findById(userProfileDto.getUserId());
            if (existingProfileOpt.isEmpty()) {
                return ApiResponse.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .success(false)
                        .message("User profile not found")
                        .build();
            }

            UserProfile existing = existingProfileOpt.get();
            BeanUtils.copyProperties(userProfileDto, existing, "userId", "userProviders");
            UserProfile saved = userProfileRepo.save(existing);

            return ApiResponse.builder()
                    .httpStatus(HttpStatus.OK)
                    .success(true)
                    .message("User profile updated successfully")
                    .data(saved)
                    .build();
        } catch (Exception e) {
            log.error("Error updating user profile for userId: {}", userProfileDto.getUserId(), e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating user profile");
        }
    }

    @Override
    public ApiResponse deleteUserProfile(UUID userId) {
        log.info("Deleting user profile for userId: {}", userId);
        try {
            if (!userProfileRepo.existsById(userId)) {
                return ApiResponse.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .success(false)
                        .message("User profile not found")
                        .build();
            }
            userProfileRepo.deleteById(userId);
            return ApiResponse.builder()
                    .httpStatus(HttpStatus.OK)
                    .success(true)
                    .message("User profile deleted successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error deleting user profile for userId: {}", userId, e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting user profile");
        }
    }

    @Override
    public ApiResponse addUserProfile(UserProfileDto userProfileDto) {
        log.info("Adding new user profile for email: {}", userProfileDto.getEmail());
        try {
            // Check if user already exists by email or mobile
            Optional<UserProfile> existingOpt = userProfileRepo.findByEmailOrMobile(
                    userProfileDto.getEmail(),
                    userProfileDto.getMobile()
            );

            UserProfile profile;
            if (existingOpt.isPresent()) {
                profile = existingOpt.get();
                BeanUtils.copyProperties(userProfileDto, profile, "userId", "userProviders");
            } else {
                profile = new UserProfile();
                BeanUtils.copyProperties(userProfileDto, profile, "userId", "userProviders");
                profile.setUserId(UUID.randomUUID());
                profile.setStatus(true);
                profile.setRole(userProfileDto.getRole() != null ? userProfileDto.getRole() : "USER");
                profile.setLastLogin(LocalDateTime.now().toString());
                if (profile.getUserProviders() == null) {
                    profile.setUserProviders(new HashSet<>());
                }
            }

            UserProfile saved = userProfileRepo.save(profile);
            return ApiResponse.builder()
                    .httpStatus(HttpStatus.CREATED)
                    .success(true)
                    .message("User profile created/updated successfully")
                    .data(saved)
                    .build();
        } catch (Exception e) {
            log.error("Error adding user profile for email: {}", userProfileDto.getEmail(), e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user profile");
        }
    }

    @Override
    public ApiResponse handleSocialLogin(SocialLoginRequestDto request) {
        log.info("Handling social login for provider: {} and providerUserId: {}", request.provider(), request.providerUserId());
        try {
            // 1. Check if we already have a user with this providerUserId
            UserProvider userProvider = userProviderRepo
                    .findByAuthProviderAndProviderId(request.provider(), request.providerUserId())
                    .orElse(null);

            UserProfile userProfile;

            if (userProvider != null) {
                // Existing user -> load their profile
                userProfile = userProvider.getUserProfile();
                // Optionally update profile fields (name, picture, etc.)
                userProfile.setFName(request.firstName());
                userProfile.setLName(request.lastName());
                userProfile.setUsername(request.email());
                userProfile.setProfilePic(request.pictureUrl());
                userProfile.setLastLogin(LocalDateTime.now().toString());
            } else {
                // 2. Create a new UserProfile
                userProfile = new UserProfile();
                userProfile.setUserId(UUID.randomUUID());  // generate your userId here
                userProfile.setUsername(request.email());
                userProfile.setFName(request.firstName());
                userProfile.setLName(request.lastName());
                userProfile.setEmail(request.email());
                userProfile.setProfilePic(request.pictureUrl());
                userProfile.setRole("USER");
                userProfile.setStatus(true);
                userProfile.setLastLogin(LocalDateTime.now().toString());

                // Initialize providers set
                userProfile.setUserProviders(new HashSet<>());

                // 3. Create UserProvider entry
                UserProvider newProvider = new UserProvider();
                newProvider.setId(UUID.randomUUID());
                newProvider.setAuthProvider(request.provider());
                newProvider.setProviderId(request.providerUserId());
                newProvider.setUserProfile(userProfile);

                userProfile.getUserProviders().add(newProvider);
            }

            // 4. Save everything
            userProfile = userProfileRepo.save(userProfile);

            // 5. Generate your own JWT using userProfile.getUserId() as subject
            String jwt = jwtService.generateTokenForUser(userProfile, request.provider());

            return ApiResponse.success(
                    Map.of(
                            "userId", userProfile.getUserId(),
                            "email", userProfile.getEmail(),
                            "firstName", userProfile.getFName(),
                            "lastName", userProfile.getLName(),
                            "pictureUrl", userProfile.getProfilePic(),
                            "jwt", jwt,
                            "provider", request.provider()
                    )
            );
        } catch (Exception e) {
            log.error("Error handling social login", e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error handling social login");
        }
    }
}
