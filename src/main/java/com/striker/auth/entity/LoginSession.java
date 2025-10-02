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
public class LoginSession extends Auditing {
    @Id
    public UUID sessionId;
    private UUID userId;
    private LocalDateTime loginTime;
    private LocalDateTime loggedOut;
    private boolean isActive; //Active session
    private boolean isPlaying; // Planning or available for joining game
    private String longitude;
    private String latitude;
}
