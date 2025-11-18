package com.striker.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.striker.auth.dto.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(@Value("${google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
        .setAudience(Collections.singletonList(clientId))
        .build();
    }

    public GoogleUserInfo verifyAndGetUser(String idTokenString) throws Exception {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        GoogleUserInfo info = new GoogleUserInfo();
        info.setGoogleUserId(payload.getSubject());
        info.setEmail(payload.getEmail());
        Object emailVerified = payload.get("email_verified");
        if (emailVerified instanceof Boolean) {
            info.setEmailVerified((Boolean) emailVerified);
        } else if (emailVerified instanceof String) {
            info.setEmailVerified(Boolean.parseBoolean((String) emailVerified));
        }
        info.setName((String) payload.get("name"));
        info.setFirstName((String) payload.get("first_name"));
        info.setLastName((String) payload.get("family_name"));
        info.setPictureUrl((String) payload.get("picture"));
        info.setProvider("GOOGLE");

        return info;
    }
}