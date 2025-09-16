package com.striker.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@ToString
@Entity
public class UserProfile extends Auditing {

    @Id
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
