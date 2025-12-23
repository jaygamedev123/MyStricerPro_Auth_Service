package com.striker.auth.service;

import com.striker.auth.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;

import java.util.Date;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;
    private final RSAPrivateKey privateKey;

    public String generateToken(UUID userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuer(props.getIssuer())
                .setAudience(props.getAudience())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + props.getExpiry()))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
