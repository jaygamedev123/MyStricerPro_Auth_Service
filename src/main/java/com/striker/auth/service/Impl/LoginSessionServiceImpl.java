package com.striker.auth.service.Impl;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.LoginSessionDto;
import com.striker.auth.entity.LoginSession;
import com.striker.auth.repos.ILoginSessionRepo;
import com.striker.auth.service.ILoginSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service("loginSessionService")
public class LoginSessionServiceImpl implements ILoginSessionService {
    private final ILoginSessionRepo iLoginSessionRepo;

    public LoginSessionServiceImpl(ILoginSessionRepo iLoginSessionRepo) {
        this.iLoginSessionRepo = iLoginSessionRepo;
    }

    @Override
    public ApiResponse createLoginUserSession(LoginSessionDto loginSessionDto) {
        log.debug("Creating login session for user ID: {}", loginSessionDto.getUserId());
        log.info("Login session details: {}", loginSessionDto);
        try {
            LoginSession entity = new LoginSession();
            BeanUtils.copyProperties(loginSessionDto, entity);

            LoginSession save = this.iLoginSessionRepo.save(entity);
            log.debug("Login session created successfully for user ID: {}", loginSessionDto);

            return ApiResponse.builder()
                    .data(save)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            log.error("Error creating login session: {}", e.getMessage());
            return ApiResponse.builder()
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage("Error creating login session: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ApiResponse getLoginUserSessions(UUID userId) {
        log.debug("Retrieving login sessions for user ID: {}", userId);
        try {
            LoginSession loginSession = this.iLoginSessionRepo.findByUserId(userId);
            if (loginSession != null) {
                return ApiResponse.builder()
                        .data(loginSession)
                        .httpStatus(HttpStatus.OK)
                        .build();
            } else {
                return ApiResponse.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .errorMessage("No active sessions found for user ID: " + userId)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error retrieving login sessions: {}", e.getMessage());
            return ApiResponse.builder()
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage("Error retrieving login sessions: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ApiResponse invalidateLoginUserSession(UUID sessionId) {
        log.debug("Invalidating login session for sessionId: {}", sessionId);
        LoginSession loginSession = this.iLoginSessionRepo.invalidateUserSession(sessionId);
        try {
            log.info("Login session invalidated for sessionId: {}", sessionId);
            if (loginSession != null) {
                return ApiResponse.builder()
                        .data(loginSession)
                        .httpStatus(HttpStatus.OK)
                        .build();
            } else {
                return ApiResponse.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .errorMessage("No active session found to invalidate for sessionId: " + sessionId)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error invalidating login session: {}", e.getMessage());
            return ApiResponse.builder()
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage("Error invalidating login session: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ApiResponse getListOfActiveSessions() {
        log.debug("Retrieving list of active login sessions");
        try {
            var activeSessions = this.iLoginSessionRepo.findAll().stream()
                    .filter(LoginSession::isActive)
                    .toList();

            return ApiResponse.builder()
                    .data(activeSessions)
                    .httpStatus(HttpStatus.OK)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving active login sessions: {}", e.getMessage());
            return ApiResponse.builder()
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorMessage("Error retrieving active login sessions: " + e.getMessage())
                    .build();
        }
    }
}
