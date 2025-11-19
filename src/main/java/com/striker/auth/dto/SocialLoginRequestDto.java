package com.striker.auth.dto;

public record SocialLoginRequestDto(
        String provider,        // e.g. "GOOGLE"
        String providerUserId,  // Google `sub`
        String email,
        String firstName,
        String lastName,
        String fullName,
        String pictureUrl
) {
}
