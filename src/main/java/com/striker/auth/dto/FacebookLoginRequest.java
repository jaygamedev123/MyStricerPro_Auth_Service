package com.striker.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FacebookLoginRequest {
    // access token received from frontend (Facebook SDK)
    private String accessToken;
}
