package com.striker.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "user_providers")
public class UserProvider extends Auditing {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Authentication provider, e.g. "LOCAL", "GOOGLE", "FACEBOOK"
     */
    @Column(name = "auth_provider", nullable = false)
    private String authProvider;

    /**
     * ID returned by provider (e.g. Google "sub").
     */
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @ManyToOne
    @JoinColumn(name = "user_profile_id", nullable = false)
    @JsonIgnore
    private UserProfile userProfile;

    public UserProvider(String authProvider, String providerId, UserProfile userProfile) {
        this.id = UUID.randomUUID();
        this.authProvider = authProvider;
        this.providerId = providerId;
        this.userProfile = userProfile;
    }
}
