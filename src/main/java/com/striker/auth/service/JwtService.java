package com.striker.auth.service;

import com.striker.auth.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private final JwtProperties props;
    private RSAPrivateKey privateKey;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        try {
            Resource resource = props.getPrivateKeyFile();

            String pem = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            String normalized = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(normalized);

            this.privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(decoded));

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize JwtService", e);
        }
    }

    // ✅ THIS is the method everyone should call
    public String generateToken(UUID userId, Map<String, Object> claims) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId.toString())
                .setIssuer(props.getIssuer())
                .setAudience(props.getAudience())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(props.getExpiry())))
                .addClaims(claims)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
