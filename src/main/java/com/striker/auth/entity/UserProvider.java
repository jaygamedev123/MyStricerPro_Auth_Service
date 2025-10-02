package com.striker.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
public class UserProvider {

    @Id
    private UUID providerDetailsId;
    private UUID userId;
    private String authProvider; // e.g., "local", "google", "facebook"
    private String providerId; // ID from the auth provider

    //    Create many to one with userProfiles4
    @ManyToOne
    @JoinColumn(name = "user_profile_id", nullable = false, insertable = true)
    private UserProfile userProfile;

    public UserProvider(String authProvider) {
        this.authProvider = authProvider;
    }

}
