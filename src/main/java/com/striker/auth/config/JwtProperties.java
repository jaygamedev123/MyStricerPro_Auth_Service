package com.striker.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    private String privateKeyFile;
    private String privateKeyB64;

    private String publicKeyFile;
    private String publicKeyB64;

    /** Key id (kid) to embed in token header. Useful for rotation. */
    private String kid = "key-1";

    /** Issuer claim */
    private String issuer = "https://auth.example.com";

    /** Audience claim (optional) */
    private String audience = "gameservice";

    /** expiry in milliseconds */
    private long expiry = 18000000L; // default 5 hours
}
