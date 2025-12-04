package com.striker.auth.service;

import com.striker.auth.config.JwtProperties;
import com.striker.auth.config.KeyUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties props;

    // signing key
    private PrivateKey privateKey;

    // verification / JWKS key
    private RSAPublicKey publicKey;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        try {
            // Load private key from file (Option A)
            if (props.getPrivateKeyFile() != null && !props.getPrivateKeyFile().isBlank()) {
                Path privPath = Path.of(props.getPrivateKeyFile());
                String privPem = KeyUtil.readFileOrNull(privPath);

                if (privPem == null || privPem.isBlank()) {
                    throw new IllegalStateException("Private key file is empty: " + privPath);
                }

                this.privateKey = KeyUtil.readPrivateKeyFromPem(privPem);
                log.info("Loaded private key from {}", privPath);
            } else {
                throw new IllegalStateException("auth.jwt.privateKeyFile is not configured.");
            }

            // Load public key from file (Option A)
            if (props.getPublicKeyFile() != null && !props.getPublicKeyFile().isBlank()) {
                Path pubPath = Path.of(props.getPublicKeyFile());
                String pubPem = KeyUtil.readFileOrNull(pubPath);

                if (pubPem == null || pubPem.isBlank()) {
                    throw new IllegalStateException("Public key file is empty: " + pubPath);
                }

                this.publicKey = (RSAPublicKey) KeyUtil.readPublicKeyFromPem(pubPem);
                log.info("Loaded public key from {}", pubPath);
            } else {
                throw new IllegalStateException("auth.jwt.publicKeyFile is not configured.");
            }

            // All good
            log.info("JwtService initialized successfully. kid={}, issuer={}, audience={}",
                    props.getKid(), props.getIssuer(), props.getAudience());
        } catch (Exception e) {
            log.error("Failed to initialize JwtService", e);
            throw new IllegalStateException("Failed to initialize JwtService", e);
        }
    }

    //Expose the RSAPublicKey for JWKS endpoint or other verifiers.

    public RSAPublicKey getPublicKey() {
        return this.publicKey;
    }

    // Create RS256-signed JWT with given claims and subject.

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        Objects.requireNonNull(privateKey, "privateKey must be initialized");

        long now = Instant.now().toEpochMilli();
        long exp = now + props.getExpiry();

        var builder = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(exp))
                .setHeaderParam("kid", props.getKid())
                .setIssuer(props.getIssuer());

        if (props.getAudience() != null && !props.getAudience().isBlank()) {
            builder.setAudience(props.getAudience());
        }

        return builder.signWith(privateKey, SignatureAlgorithm.RS256).compact();
    }
}
