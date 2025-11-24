package com.striker.auth.service.Impl;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.SocialLoginRequestDto;
import com.striker.auth.dto.UserProfileDto;
import com.striker.auth.dto.GuestLoginRequestDto;
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
import java.util.*;

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
                userProfile.setUsername(request.email());
                userProfile.setProfilePic(request.pictureUrl());
                userProfile.setLastLogin(LocalDateTime.now().toString());
            } else {
                // 2. Create a new UserProfile
                userProfile = new UserProfile();
                userProfile.setUserId(UUID.randomUUID());  // generate your userId here
                userProfile.setUsername(request.email());
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
                            "jwt", jwt,
                            "provider", request.provider()
                    )
            );
        } catch (Exception e) {
            log.error("Error handling social login", e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error handling social login");
        }
    }

    @Override
    public ApiResponse handleGuestLogin(GuestLoginRequestDto request) {

        try {
            // 1. Create UserProfile
            UserProfile user = new UserProfile();
            UUID uuid = UUID.randomUUID();

            user.setUserId(uuid);
            user.setRole("GUEST");
            user.setStatus(true);
            user.setLastLogin(LocalDateTime.now().toString());

            // username like Guest-3F9A12CD
            String shortId = uuid.toString().substring(0, 8).toUpperCase();
            user.setUsername("Guest-" + shortId);

            // 2. Create UserProvider
            UserProvider provider = new UserProvider();
            provider.setId(UUID.randomUUID());
            provider.setAuthProvider("GUEST");
            provider.setProviderId(uuid.toString()); // simple approach
            provider.setUserProfile(user);

            user.setUserProviders(Set.of(provider));

            // 3. Save user
            user = userProfileRepo.save(user);

            // 4. Create JWT
            String jwt = jwtService.generateTokenForUser(user, "GUEST");

            // 5. Response
            return ApiResponse.success(
                    Map.of(
                            "userId", user.getUserId(),
                            "username", user.getUsername(),
                            "provider", "GUEST",
                            "jwt", jwt,
                            "isGuest", true
                    )
            );

        } catch (Exception e) {
            log.error("Error creating guest user", e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Guest login failed");
        }
    }

    @Override
    public ApiResponse updateProfilePic(UUID userId, String profilePicUrl) {
        try {
            var userOpt = userProfileRepo.findById(userId);
            if (userOpt.isEmpty()) {
                return ApiResponse.error(HttpStatus.NOT_FOUND, "User not found");
            }

            UserProfile user = userOpt.get();
            user.setProfilePic(profilePicUrl);
            userProfileRepo.save(user);

            return ApiResponse.success(
                    Map.of(
                            "userId", user.getUserId(),
                            "profilePic", user.getProfilePic()
                    )
            );

        } catch (Exception e) {
            log.error("Error updating profile pic for userId: {}", userId, e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update profile picture");
        }
    }
}
