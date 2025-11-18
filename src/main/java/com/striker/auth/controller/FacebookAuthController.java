package com.striker.auth.controller;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.FacebookLoginRequest;
import com.striker.auth.dto.FacebookUserInfo;
import com.striker.auth.service.FacebookAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class FacebookAuthController {
    private final FacebookAuthService facebookAuthService;

    @PostMapping("/facebook")
    public ResponseEntity<ApiResponse> loginWithFacebook(@RequestBody FacebookLoginRequest request) {
        log.info("Received Facebook login request");
        try {
            FacebookUserInfo info = facebookAuthService.verifyAndGetUser(request.getAccessToken());

            ApiResponse response = ApiResponse.builder()
                    .httpStatus(HttpStatus.OK)
                    .success(true)
                    .message("Facebook token verified successfully")
                    .data(info)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying Facebook token: {}", e.getMessage());
            ApiResponse response = ApiResponse.builder()
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .success(false)
                    .errorMessage("Invalid Facebook token: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
