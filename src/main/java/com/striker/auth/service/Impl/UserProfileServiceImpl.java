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
import org.springframework.dao.DataIntegrityViolationException;
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
    public ApiResponse updateUserProfile(UserProfileDto dto) {
        log.info("Updating user profile for userId: {}", dto.getUserId());

        if (dto.getUserId() == null) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "userId is required for update");
        }

        try {
            Optional<UserProfile> opt = userProfileRepo.findById(dto.getUserId());
            if (opt.isEmpty()) {
                return ApiResponse.error(HttpStatus.NOT_FOUND, "User profile not found");
            }

            UserProfile existing = opt.get();

            // Email update (only if provided AND not empty)
            if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
                String newEmail = dto.getEmail().trim().toLowerCase(Locale.ROOT);

                if (!newEmail.equalsIgnoreCase(existing.getEmail())) {
                    if (userProfileRepo.findByEmail(newEmail).isPresent()) {
                        return ApiResponse.error(HttpStatus.CONFLICT, "Email already in use");
                    }
                    existing.setEmail(newEmail);
                }
            }

            // Username update (validate uniqueness)
            if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
                String newUsername = dto.getUsername().trim();

                if (!newUsername.equals(existing.getUsername())) {
                    if (userProfileRepo.existsByUsername(newUsername)) {
                        return ApiResponse.error(HttpStatus.CONFLICT, "Username already in use");
                    }
                    existing.setUsername(newUsername);
                }
            }

            // Full name
            if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
                existing.setFullName(dto.getFullName().trim());
            }

            // Mobile
            if (dto.getMobile() != null && !dto.getMobile().trim().isEmpty()) {
                existing.setMobile(dto.getMobile().trim());
            }

            // Sex
            if (dto.getSex() != null && !dto.getSex().trim().isEmpty()) {
                existing.setSex(dto.getSex().trim());
            }

            // Profile Pic
            if (dto.getProfilePic() != null && !dto.getProfilePic().trim().isEmpty()) {
                existing.setProfilePic(dto.getProfilePic());
            }

            // DOB
            if (dto.getDob() != null && !dto.getDob().trim().isEmpty()) {
                existing.setDob(dto.getDob().trim());
            }

            // Role — only allow change if provided
            if (dto.getRole() != null && !dto.getRole().trim().isEmpty()) {
                existing.setRole(dto.getRole().trim());
            }


            // Password — update only if provided
            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                existing.setPassword(dto.getPassword().trim());
            }

            UserProfile saved = userProfileRepo.save(existing);

            return ApiResponse.success(saved);

        } catch (DataIntegrityViolationException ex) {
            log.warn("Unique constraint violation updating user {}", dto.getUserId(), ex);
            return ApiResponse.error(HttpStatus.CONFLICT,
                    "Duplicate email or username");
        } catch (Exception e) {
            log.error("Error updating user profile for userId: {}", dto.getUserId(), e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error updating user profile");
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
            // Check if user already exists by email
            Optional<UserProfile> existingOpt = userProfileRepo.findByEmail(
                    userProfileDto.getEmail()
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
        log.info("Handling social login for provider: {} and providerUserId: {}",
                request.provider(), request.providerUserId());
        try {
            String email = request.email();
            if (email == null || email.isBlank()) {
                return ApiResponse.error(HttpStatus.BAD_REQUEST,
                        "Email is required for social login");
            }

            // Find existing provider mapping
            UserProvider existingProvider = userProviderRepo
                    .findByAuthProviderAndProviderId(request.provider(), request.providerUserId())
                    .orElse(null);

            UserProfile userProfile;

            if (existingProvider != null) {
                // Provider exists → Update and return existing user
                userProfile = existingProvider.getUserProfile();

                // If incoming provider sent a username and profile has none, set it (sanitized + unique)
                String incomingUsername = request.username();
                if ((userProfile.getUsername() == null || userProfile.getUsername().isBlank())
                        && incomingUsername != null && !incomingUsername.isBlank()) {
                    userProfile.setUsername(
                            generateUniqueUsername(incomingUsername, request.email(), request.fullName(), userProfile.getUserId())
                    );
                }

                // Update optional fields only if provided (avoid clearing)
                if (request.pictureUrl() != null && !request.pictureUrl().isBlank()) {
                    userProfile.setProfilePic(request.pictureUrl());
                }
                if (request.fullName() != null && !request.fullName().isBlank()
                        && (userProfile.getFullName() == null || userProfile.getFullName().isBlank())) {
                    userProfile.setFullName(request.fullName());
                }
                userProfile.setLastLogin(LocalDateTime.now().toString());
            } else {
                // No provider mapping → Check if email already exists
                Optional<UserProfile> emailUserOpt = userProfileRepo.findByEmail(email);
                if (emailUserOpt.isPresent()) {
                    // Email exists → BLOCK THE USER CREATION
                    return ApiResponse.error(HttpStatus.CONFLICT,
                            "Email already exists. Please login with correct provider.");
                }

                // Email is UNIQUE → Create new user
                userProfile = new UserProfile();
                UUID newUserId = UUID.randomUUID();
                userProfile.setUserId(newUserId);
                userProfile.setEmail(email);
                userProfile.setFullName(request.fullName());
                userProfile.setProfilePic(request.pictureUrl());
                userProfile.setRole("USER");
                userProfile.setStatus(true);
                userProfile.setLastLogin(LocalDateTime.now().toString());
                userProfile.setUserProviders(new HashSet<>());

                // Generate username: prefer provider's username if present, otherwise fullName/email
                userProfile.setUsername(
                        generateUniqueUsername(request.username(), request.email(), request.fullName(), newUserId)
                );

                // Create provider link
                UserProvider newProvider = new UserProvider();
                newProvider.setId(UUID.randomUUID());
                newProvider.setAuthProvider(request.provider());
                newProvider.setProviderId(request.providerUserId());
                newProvider.setUserProfile(userProfile);

                userProfile.getUserProviders().add(newProvider);
            }

            // Save user
            userProfile = userProfileRepo.save(userProfile);

            // build token claims you want to include
            Map<String, Object> claims = Map.of(
                    "email", userProfile.getEmail(),
                    "fullname", userProfile.getFullName(),
                    "provider", request.provider()
                    // add other custom claims if needed, e.g. "roles": List.of("player")
            );

            // generate RS256 token (JwtService reads private key and signs)
            String jwt = jwtService.generateToken(userProfile.getUserId());

            return ApiResponse.success(
                    Map.of(
                            "userId", userProfile.getUserId(),
                            "email", userProfile.getEmail(),
                            "fullname", userProfile.getFullName(),
                            "jwt", jwt,
                            "provider", request.provider()
                    )
            );

        } catch (DataIntegrityViolationException ex) {
            // In case DB unique constraint catches duplicates
            return ApiResponse.error(HttpStatus.CONFLICT, "Email already exists");
        } catch (Exception e) {
            log.error("Error handling social login", e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error handling social login");
        }
    }



    @Override
    public ApiResponse handleGuestLogin(GuestLoginRequestDto request) {

        try {
            // Create UserProfile
            UserProfile user = new UserProfile();
            UUID uuid = UUID.randomUUID();

            user.setUserId(uuid);
            user.setRole("GUEST");
            user.setStatus(true);
            user.setLastLogin(LocalDateTime.now().toString());

            // username like Guest-3F9A12CD
            String shortId = uuid.toString().substring(0, 8).toUpperCase();
            user.setUsername("Guest-" + shortId);

            // Create UserProvider
            UserProvider provider = new UserProvider();
            provider.setId(UUID.randomUUID());
            provider.setAuthProvider("GUEST");
            provider.setProviderId(uuid.toString()); // simple approach
            provider.setUserProfile(user);

            user.setUserProviders(Set.of(provider));

            // Save user
            user = userProfileRepo.save(user);

            // Build claims for the JWT
            Map<String, Object> claims = Map.of(
                    "username", user.getUsername(),
                    "provider", "GUEST",
                    "isGuest", true
            );


            // Generate RS256 JWT
            String jwt = jwtService.generateToken(user.getUserId());

            // Response
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

    private String generateUniqueUsername(String incomingUsername, String email, String fullName, UUID userId) {
        // 1) If user exists and already has username, preserve it.
        if (userId != null) {
            Optional<UserProfile> existingOpt = userProfileRepo.findById(userId);
            if (existingOpt.isPresent()) {
                String existingUsername = existingOpt.get().getUsername();
                if (existingUsername != null && !existingUsername.isBlank()) {
                    return existingUsername;
                }
            }
        }

        // Try incoming username first
        String base = sanitizeUsername(incomingUsername);

        // Then fullName
        if ((base == null || base.isBlank()) && fullName != null && !fullName.isBlank()) {
            base = sanitizeUsername(fullName);
        }

        // Then email local-part
        if (base == null || base.isBlank()) {
            String local = (email != null && email.contains("@")) ? email.substring(0, email.indexOf("@")) : email;
            base = sanitizeUsername(local);
            if (base == null || base.isBlank()) {
                base = "user" + UUID.randomUUID().toString().substring(0, 8);
            }
        }

        // final trim/collapse
        base = base.replaceAll("^\\.+", "").replaceAll("\\.+$", "");
        if (base.isBlank()) {
            base = "user" + UUID.randomUUID().toString().substring(0, 8);
        }

        // ensure uniqueness
        String candidate = base;
        int counter = 1;
        while (userProfileRepo.existsByUsername(candidate)) {
            candidate = base + "_" + counter;
            counter++;
        }
        return candidate;
    }

    private String sanitizeUsername(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        // replace spaces with dots
        s = s.replaceAll("\\s+", ".");
        // allow only a-z0-9 . _ - characters
        s = s.replaceAll("[^a-z0-9._-]", "");
        // collapse repeated separators to a single dot
        s = s.replaceAll("[._-]{2,}", ".");
        // trim leading/trailing separators
        s = s.replaceAll("^[._-]+", "").replaceAll("[._-]+$", "");
        // enforce max length (optional)
        if (s.length() > 30) s = s.substring(0, 30);
        return s;
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

    private static Map<String, Object> buildSafeMap(Object... keysAndValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i + 1 < keysAndValues.length; i += 2) {
            String key = (String) keysAndValues[i];
            Object val = keysAndValues[i + 1];
            if (key == null) continue;
            if (val != null) map.put(key, val);
        }
        return Collections.unmodifiableMap(map);
    }
}
