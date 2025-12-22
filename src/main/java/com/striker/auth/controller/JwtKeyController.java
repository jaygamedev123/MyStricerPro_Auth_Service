package com.striker.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RestController
@RequestMapping("/.well-known")
public class JwtKeyController {

    private final RSAPublicKey publicKey;

    public JwtKeyController(
            @Value("${auth.jwt.publicKeyFile}") Resource publicKeyResource
    ) throws Exception {
        String pem = new String(publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        String normalized = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(normalized);

        this.publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }

    @GetMapping("/public-key")
    public String publicKey() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
