package com.striker.auth.service;

import com.striker.auth.entity.UserProfile;
import com.striker.auth.entity.UserProvider;
import com.striker.auth.repos.IUserProfileRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final IUserProfileRepo userProfileRepo;
    private final JwtService jwtService;   // your existing JwtService

    public AuthResponse handleSocialLogin(
            String provider,        // "GOOGLE" / "FACEBOOK"
            String providerUserId,  // sub (google id) or facebook id
            String email,
            String firstName,
            String lastName,
            String pictureUrl
    ) {

        // 1. Find or create user by email
        Optional<UserProfile> existingOpt = userProfileRepo.findByEmail(email);
        UserProfile user;

        if (existingOpt.isPresent()) {
            user = existingOpt.get();
        } else {
            user = new UserProfile();
            user.setUserId(UUID.randomUUID());
            user.setEmail(email);
            user.setUsername(email != null ? email.split("@")[0] : providerUserId);
            user.setFName(firstName);
            user.setLName(lastName);
            user.setProfilePic(pictureUrl);
            user.setRole("USER");
            user.setLastLogin(LocalDateTime.now().toString());
            user.setUserProviders(new HashSet<>());
        }

        // 2. Ensure provider mapping exists
        if (user.getUserProviders() == null) {
            user.setUserProviders(new HashSet<>());
        }

        boolean alreadyLinked = user.getUserProviders().stream().anyMatch(up ->
                provider.equalsIgnoreCase(up.getAuthProvider())
                        && providerUserId.equals(up.getProviderId())
        );

        if (!alreadyLinked) {
            UserProvider up = new UserProvider();
            up.setUserId(user.getUserId());
            up.setAuthProvider(provider);
            up.setProviderId(providerUserId);
            up.setUserProfile(user);
            user.getUserProviders().add(up);
        }

        user.setLastLogin(LocalDateTime.now().toString());
        user = userProfileRepo.save(user);

        // 3. Issue your own JWT
        String token = jwtService.generateToken(user, provider); // adapt to your JwtService

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                provider
        );
    }

    // simple response DTO, or put it in its own file
    public record AuthResponse(
            String accessToken,
            UUID userId,
            String username,
            String email,
            String provider
    ) {}
}
