package com.striker.auth.dto;

import com.striker.auth.entity.Auditing;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class UserProfileDto  {
    private UUID userId;
    private String username;
    private String fName;
    private String lName;
    private String mobile;
    private String email;
    private String password;
    private boolean status;
    private String profilePic;
    private String lastLogin;
    private String authProvider; // e.g., "local", "google", "facebook"
    private String providerId; // ID from the auth provider
    private String role; // e.g., "USER", "ADMIN"
    private LocalDateTime dob;

}
