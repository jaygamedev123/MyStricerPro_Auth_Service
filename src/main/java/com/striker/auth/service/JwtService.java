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

/**
 * Simple JWT generator.
 *
 * In a real production system you would load the secret key from configuration
 * (e.g. environment variable) so it is stable across restarts.
 */
@Service
public class JwtService {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateTokenForUser(UserProfile user, String provider) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("provider", provider);
        claims.put("userId", user.getUserId().toString());

        long now = System.currentTimeMillis();
        long expiryMillis = now + 1000L * 60 * 60 * 5; // 5 hours

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUserId().toString())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryMillis))
                .signWith(key)
                .compact();
    }

    public String generateToken(String subject) {
        long now = System.currentTimeMillis();
        long expiryMillis = now + 1000L * 60 * 60 * 5;

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryMillis))
                .signWith(key)
                .compact();
    }
}
