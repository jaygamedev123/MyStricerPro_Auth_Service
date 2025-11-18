package com.striker.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GoogleUserInfo {
    private String googleUserId;
    private String email;
    private boolean emailVerified;
    private String name;
    private String firstName;
    private String lastName;
    private String pictureUrl;
    private String provider; // e.g. GOOGLE
}