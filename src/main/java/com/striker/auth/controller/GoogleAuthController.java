package com.striker.auth.controller;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.GoogleLoginRequest;
import com.striker.auth.dto.GoogleUserInfo;
import com.striker.auth.service.GoogleAuthService;
import com.striker.auth.service.SocialLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final SocialLoginService socialLoginService;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        try {
            GoogleUserInfo info = googleAuthService.verifyAndGetUser(request.getIdToken());

            var authResponse = socialLoginService.handleSocialLogin(
                    info.getProvider(),
                    info.getGoogleUserId(),
                    info.getEmail(),
                    info.getFirstName(),
                    info.getLastName(),
                    info.getPictureUrl()
            );

            ApiResponse response = ApiResponse.builder()
                    .httpStatus(HttpStatus.OK)
                    .success(true)
                    .message("Google login successful")
                    .data(authResponse)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = ApiResponse.builder()
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .success(false)
                    .errorMessage("Invalid Google token: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
