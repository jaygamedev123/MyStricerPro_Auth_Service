package com.striker.auth.service;

import com.striker.auth.dto.FacebookUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class FacebookAuthService {
    @Value("${facebook.app-id}")
    private String appId;

    @Value("${facebook.app-secret}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public FacebookUserInfo verifyAndGetUser(String userAccessToken) {
        // 1) Verify access token with debug_token
        String appAccessToken = appId + "|" + appSecret;

        String debugUrl = "https://graph.facebook.com/debug_token" +
                "?input_token=" + userAccessToken +
                "&access_token=" + appAccessToken;

        Map<String, Object> debugResponse = restTemplate.getForObject(debugUrl, Map.class);
        if (debugResponse == null || !debugResponse.containsKey("data")) {
            throw new IllegalArgumentException("Invalid response from Facebook debug_token API");
        }

        Map<String, Object> data = (Map<String, Object>) debugResponse.get("data");

        Boolean isValid = (Boolean) data.get("is_valid");
        String appIdFromFb = (String) data.get("app_id");

        if (isValid == null || !isValid || !appId.equals(appIdFromFb)) {
            throw new IllegalArgumentException("Invalid Facebook access token");
        }

        // 2) Fetch user profile from /me
        String meUrl = "https://graph.facebook.com/me" +
                "?fields=id,name,email,picture" +
                "&access_token=" + userAccessToken;

        Map<String, Object> meResponse = restTemplate.getForObject(meUrl, Map.class);
        if (meResponse == null || !meResponse.containsKey("id")) {
            throw new IllegalArgumentException("Unable to fetch Facebook user profile");
        }

        FacebookUserInfo info = new FacebookUserInfo();
        info.setFacebookUserId((String) meResponse.get("id"));
        info.setName((String) meResponse.get("name"));
        info.setEmail((String) meResponse.get("email"));
        info.setProvider("FACEBOOK");

        // Picture is nested like: picture -> data -> url
        Object pictureObj = meResponse.get("picture");
        if (pictureObj instanceof Map) {
            Map<String, Object> pictureMap = (Map<String, Object>) pictureObj;
            Object dataObj = pictureMap.get("data");
            if (dataObj instanceof Map) {
                Map<String, Object> picData = (Map<String, Object>) dataObj;
                Object urlObj = picData.get("url");
                if (urlObj instanceof String) {
                    info.setPictureUrl((String) urlObj);
                }
            }
        }

        return info;
    }
}
