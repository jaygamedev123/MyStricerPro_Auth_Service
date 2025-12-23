package com.striker.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RestController
@RequestMapping("/.well-known")
public class JwtKeyController {

    private final RSAPublicKey publicKey;

    public JwtKeyController(
            @Value("${auth.jwt.publicKeyFile:}") Resource publicKeyFile,
            @Value("${auth.jwt.publicKeyB64:}") String publicKeyB64
    ) throws Exception {

        String pem;

        // Prefer file (local dev)
        if (publicKeyFile != null && publicKeyFile.exists()) {
            pem = new String(
                    publicKeyFile.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
        // Fallback to Base64 (AWS / Docker)
        else if (publicKeyB64 != null && !publicKeyB64.isBlank()) {
            pem = publicKeyB64;
        }
        // Fail fast
        else {
            throw new IllegalStateException(
                    "No public key configured. Provide auth.jwt.publicKeyFile OR auth.jwt.publicKeyB64"
            );
        }

        // Normalize PEM or raw base64
        String normalized = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(normalized);

        this.publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }

    /**
     * Exposed endpoint for game-service & other services
     */
    @GetMapping("/public-key")
    public String publicKey() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
