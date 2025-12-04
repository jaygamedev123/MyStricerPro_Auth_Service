package com.striker.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@ToString
@Entity
@Table(name = "user_profiles")
public class UserProfile extends Auditing {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    private String username;
    private String fullName;
    private String mobile;
    private String sex;
    private String profilePic;
    @Column(unique = true)
    private String email;
    private String dob;
    private String lastLogin;
    private String role;        // e.g., "USER", "ADMIN"
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean status;     // active / inactive
    private String password;    // optional; for social login can stay null

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<UserProvider> userProviders = new HashSet<>();
}
