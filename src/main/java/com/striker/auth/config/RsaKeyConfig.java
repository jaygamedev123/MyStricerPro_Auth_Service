package com.striker.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@Slf4j
public class RsaKeyConfig {

    @Bean
    public RSAPrivateKey rsaPrivateKey(
            @Value("${auth.jwt.privateKeyB64}") String privateKeyPem
    ) throws Exception {

        byte[] keyBytes = Base64.getDecoder().decode(
                privateKeyPem
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s+", "")
        );

        RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

        return privateKey;
    }

    @Bean
    public RSAPublicKey rsaPublicKey(
            @Value("${auth.jwt.publicKeyB64}") String publicKeyPem
    ) throws Exception {

        byte[] keyBytes = Base64.getDecoder().decode(
                publicKeyPem
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s+", "")
        );

        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(keyBytes));

        // 🔑 ADD THIS LINE (THIS IS THE FIX)
        logPublicKeyFingerprint("AUTH", publicKey);

        return publicKey;
    }

    private void logPublicKeyFingerprint(String service, RSAPublicKey key) {
        try {
            String fingerprint = Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256")
                            .digest(key.getEncoded())
            );
            log.info("🔑 {} PUBLIC KEY SHA-256 = {}", service, fingerprint);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
