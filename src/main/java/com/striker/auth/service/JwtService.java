package com.striker.auth.service;

import com.striker.auth.entity.UserProfile;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // Use a long, random secret in real app – move to config
    private final Key key = Keys.hmacShaKeyFor(
            "very-secret-key-for-jwt-signing-please-change-me-123456".getBytes()
    );

    public String generateToken(UserProfile user, String provider) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("provider", provider);
        claims.put("userId", user.getUserId().toString());

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 1000 * 60 * 60 * 5)) // 5 hours
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
