package com.striker.auth.controller;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.LoginSessionDto;
import com.striker.auth.service.ILoginSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/loginsessions")
public class LoginSessionController {

    private final ILoginSessionService loginSessionService;

    public LoginSessionController(ILoginSessionService loginSessionService) {
        this.loginSessionService = loginSessionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createLoginUserSession(@RequestBody LoginSessionDto loginSessionDto) {
        log.debug("Creating login session for userId: {}", loginSessionDto.getUserId());
        return ResponseEntity.ok(loginSessionService.createLoginUserSession(loginSessionDto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getLoginUserSessions(@PathVariable UUID userId) {
        log.debug("Fetching login sessions for userId: {}", userId);
        return ResponseEntity.ok(loginSessionService.getLoginUserSessions(userId));
    }

    @PutMapping("/{sessionId}/invalidate")
    public ResponseEntity<ApiResponse> invalidateLoginUserSession(@PathVariable UUID sessionId) {
        log.debug("Invalidating login session with sessionId: {}", sessionId);
        return ResponseEntity.ok(loginSessionService.invalidateLoginUserSession(sessionId));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getListOfActiveSessions() {
        log.debug("Fetching list of active sessions");
        return ResponseEntity.ok(loginSessionService.getListOfActiveSessions());
    }
}
