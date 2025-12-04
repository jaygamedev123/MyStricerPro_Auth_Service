package com.striker.auth.web;

import com.striker.auth.config.JwtProperties;
import com.striker.auth.config.KeyUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class PublicKeyController {

    private final JwtProperties props;
    private final RSAPublicKey rsaPublicKey;

    public PublicKeyController(JwtProperties props) throws Exception {
        this.props = props;

        // load public key from B64 or file
        String pem = null;
        if (props.getPublicKeyB64() != null && !props.getPublicKeyB64().isBlank()) {
            pem = new String(java.util.Base64.getDecoder().decode(props.getPublicKeyB64()));
        } else if (props.getPublicKeyFile() != null && !props.getPublicKeyFile().isBlank()) {
            pem = KeyUtil.readFileOrNull(Path.of(props.getPublicKeyFile()));
        }

        if (pem == null) {
            throw new IllegalStateException("No public key configured for JWKS endpoint. Set auth.jwt.publicKeyFile or auth.jwt.publicKeyB64");
        }

        PublicKey pub = KeyUtil.readPublicKeyFromPem(pem);
        if (!(pub instanceof RSAPublicKey)) {
            throw new IllegalStateException("Public key is not RSA");
        }
        this.rsaPublicKey = (RSAPublicKey) pub;
    }

    // Returns the PEM as plain text for services that prefer raw PEM.

    @GetMapping(value = "/.well-known/jwks.pem", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getPem() {
        String pem;
        if (props.getPublicKeyB64() != null && !props.getPublicKeyB64().isBlank()) {
            pem = new String(java.util.Base64.getDecoder().decode(props.getPublicKeyB64()));
        } else {
            pem = KeyUtil.readFileOrNull(Path.of(props.getPublicKeyFile()));
        }
        return ResponseEntity.ok(pem);
    }

    // Minimal JWKS JSON: {"keys":[ { kty, kid, use, alg, n, e } ]}
    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getJwks() {
        Map<String, Object> jwk = KeyUtil.rsaPublicKeyToJwk(rsaPublicKey, props.getKid());
        return ResponseEntity.ok(Collections.singletonMap("keys", List.of(jwk)));
    }
}
