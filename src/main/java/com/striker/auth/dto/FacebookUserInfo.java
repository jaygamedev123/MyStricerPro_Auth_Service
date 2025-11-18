package com.striker.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FacebookUserInfo {
    private String facebookUserId;
    private String email;
    private String firstName;
    private String lastName;
    private String name;
    private String pictureUrl;
    private String provider;
}
