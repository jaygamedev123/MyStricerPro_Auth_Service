package com.striker.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    private Resource privateKeyFile;
    private Resource publicKeyFile;
    private String issuer;
    private String audience;
    private String kid;
    private long expiry;

}
