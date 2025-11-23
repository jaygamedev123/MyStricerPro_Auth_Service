package com.striker.auth.dto;

public record SocialLoginRequestDto(
        String provider,        // e.g. "GOOGLE"
        String providerUserId,  // Google `sub`
        String email,
        String fullName,
        String pictureUrl
) {
}
