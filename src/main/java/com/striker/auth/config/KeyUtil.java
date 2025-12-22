package com.striker.auth.config;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

//Utilities to read PKCS#8 private key PEM and X.509 public key PEM.

public class KeyUtil {

    public static PrivateKey readPrivateKeyFromPem(String pem) throws Exception {
        String normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey readPublicKeyFromPem(String pem) throws Exception {
        String normalized = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static String readFileOrNull(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Convert RSAPublicKey to a minimal JWK map with "kty","kid","use","n","e".
     * n,e are Base64URL (no padding).
     */
    public static Map<String, Object> rsaPublicKeyToJwk(RSAPublicKey pub, String kid) {
        Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();

        BigInteger modulus = pub.getModulus();
        BigInteger exponent = pub.getPublicExponent();

        String n = urlEncoder.encodeToString(stripLeadingZero(modulus.toByteArray()));
        String e = urlEncoder.encodeToString(stripLeadingZero(exponent.toByteArray()));

        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("kid", kid);
        jwk.put("use", "sig");
        jwk.put("alg", "RS256");
        jwk.put("n", n);
        jwk.put("e", e);
        return jwk;
    }

    private static byte[] stripLeadingZero(byte[] input) {
        // BigInteger.toByteArray may include a leading zero sign byte; remove it for correct encoding.
        int start = 0;
        while (start < input.length - 1 && input[start] == 0) {
            start++;
        }
        byte[] out = new byte[input.length - start];
        System.arraycopy(input, start, out, 0, out.length);
        return out;
    }
}
