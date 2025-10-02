package com.striker.auth.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;
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
    private String profilePic;
    private String email;
    private LocalDateTime dob;
    private String lastLogin;
    private String role; // e.g., "USER", "ADMIN"
    //    create one to many with userProvider

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserProvider> userProviders;
}
