package com.striker.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@ToString
public class LoginSessionDto {
    public UUID sessionId;
    private UUID userId;
    private LocalDateTime loginTime;
    private LocalDateTime loggedOut;
    private boolean isActive; //Active session
    private boolean isPlaying; // Planning or available for joining game
    private String longitude;
    private String latitude;
}
