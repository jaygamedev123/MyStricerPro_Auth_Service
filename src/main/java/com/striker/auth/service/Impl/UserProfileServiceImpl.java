package com.striker.auth.service.Impl;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.UserProfileDto;
import com.striker.auth.entity.UserProfile;
import com.striker.auth.entity.UserProvider;
import com.striker.auth.repos.IUserProfileRepo;
import com.striker.auth.service.IUserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service("userProfileService")
public class UserProfileServiceImpl implements IUserProfileService {
    private final IUserProfileRepo iUserProfileRepo;

    public UserProfileServiceImpl(IUserProfileRepo iUserProfileRepo) {
        log.debug("UserProfileServiceImpl initialized");
        this.iUserProfileRepo = iUserProfileRepo;
    }

    @Override
    public ApiResponse getUserProfile(UUID userId) {
        log.info("Fetching user profile for userId: {}", userId);
        try {
            var userProfile = iUserProfileRepo.findById(userId);
            if (userProfile.isPresent()) {
                log.info("User profile found for userId: {}", userId);
                return ApiResponse.builder().httpStatus(HttpStatus.OK).message("User profile fetched successfully").data(userProfile.get()).build();
            } else {
                log.warn("User profile not found for userId: {}", userId);
                return ApiResponse.builder().httpStatus(HttpStatus.NOT_FOUND).message("User profile not found").build();
            }
        } catch (Exception e) {
            log.error("Error fetching user profile for userId: {}", userId, e);
            return ApiResponse.builder().httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).message("Error fetching user profile").build();
        }
    }

    @Override
    public ApiResponse updateUserProfile(UserProfileDto userProfileDto) {
        log.info("Updating user profile for userId: {}", userProfileDto.getUserId());
        try {
            var existingProfile = iUserProfileRepo.findById(userProfileDto.getUserId());
            if (existingProfile.isPresent()) {
                var userProfile = existingProfile.get();
                BeanUtils.copyProperties(userProfileDto, userProfile);
                var updatedProfile = iUserProfileRepo.save(userProfile);
                log.info("User profile updated successfully for userId: {}", userProfileDto.getUserId());
                return ApiResponse.builder().httpStatus(HttpStatus.OK).message("User profile updated successfully").data(updatedProfile).build();
            } else {
                log.warn("User profile not found for userId: {}", userProfileDto.getUserId());
                return ApiResponse.builder().httpStatus(HttpStatus.NOT_FOUND).message("User profile not found").build();
            }
        } catch (Exception e) {
            log.error("Error updating user profile for userId: {}", userProfileDto.getUserId(), e);
            return ApiResponse.builder().httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).message("Error updating user profile").build();
        }
    }

    //TODO: Soft delete implementation ?
    @Override
    public ApiResponse deleteUserProfile(UUID userId) {
        log.info("Deleting user profile for userId: {}", userId);
        try {
            var existingProfile = iUserProfileRepo.findById(userId);
            if (existingProfile.isPresent()) {
                iUserProfileRepo.deleteById(userId);
                log.info("User profile deleted successfully for userId: {}", userId);
                return ApiResponse.builder().httpStatus(HttpStatus.OK).message("User profile deleted successfully").build();
            } else {
                log.warn("User profile not found for userId: {}", userId);
                return ApiResponse.builder().httpStatus(HttpStatus.NOT_FOUND).message("User profile not found").build();
            }
        } catch (Exception e) {
            log.error("Error deleting user profile for userId: {}", userId, e);
            return ApiResponse.builder().httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).message("Error deleting user profile").build();
        }

    }

    @Override
    public ApiResponse addUserProfile(UserProfileDto userProfile) {
        log.info("Adding new user profile for username: {}", userProfile.getUsername());
        var newUserProfile = new UserProfile();
        try {
            var existingProfile = iUserProfileRepo.findByEmailOrMobile(userProfile.getEmail(), userProfile.getMobile());
            if (existingProfile.isPresent()) {
                log.warn("User profile already exists with email: {} or mobile: {}", userProfile.getEmail(), userProfile.getMobile());
                existingProfile.get().getUserProviders().add(new UserProvider(userProfile.getAuthProvider()));
                newUserProfile = existingProfile.get();
            } else {
                BeanUtils.copyProperties(userProfile, newUserProfile);
                newUserProfile.getUserProviders().add(new UserProvider(userProfile.getAuthProvider()));
            }

            var savedProfile = iUserProfileRepo.save(newUserProfile);

            log.info("User profile added successfully for username: {}", userProfile.getUsername());
            return ApiResponse.builder().httpStatus(HttpStatus.CREATED).message("User profile created successfully").data(savedProfile).build();
        } catch (Exception e) {
            log.error("Error adding user profile for username: {}", userProfile.getUsername(), e);
            return ApiResponse.builder().httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).message("Error creating user profile").build();
        }
    }
}
