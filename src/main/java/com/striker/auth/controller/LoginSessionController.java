package com.striker.auth.controller;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.LoginSessionDto;
import com.striker.auth.service.ILoginSessionService;
import com.striker.auth.service.Impl.LoginSessionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/login-sessions")
public class LoginSessionController {
    private final ILoginSessionService iLoginSessionService;

    public LoginSessionController(LoginSessionServiceImpl loginSessionService) {
        this.iLoginSessionService = loginSessionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createLoginUserSession(@RequestBody LoginSessionDto loginSessionDto) {
        log.debug("Creating login session: {}", loginSessionDto);
        ApiResponse apiResponse = iLoginSessionService.createLoginUserSession(loginSessionDto);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping ("/{userId}")
    public ResponseEntity<ApiResponse> getLoginUserSessions(@PathVariable UUID userId) {
        log.debug("Fetching login sessions for userId: {}", userId);
        return ResponseEntity.ok(iLoginSessionService.getLoginUserSessions(userId));
    }

    @PutMapping
    public ResponseEntity<ApiResponse> invalidateLoginUserSession(UUID sessionId) {
        log.debug("Invalidating login session with sessionId: {}", sessionId);
        return ResponseEntity.ok(iLoginSessionService.invalidateLoginUserSession(sessionId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getListOfActiveSessions() {
        log.debug("Fetching list of active sessions");
        return ResponseEntity.ofNullable(iLoginSessionService.getListOfActiveSessions());
    }
}
