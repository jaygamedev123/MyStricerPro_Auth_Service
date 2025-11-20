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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service("loginSessionService")
public class LoginSessionServiceImpl implements ILoginSessionService {

    private final ILoginSessionRepo loginSessionRepo;

    public LoginSessionServiceImpl(ILoginSessionRepo loginSessionRepo) {
        this.loginSessionRepo = loginSessionRepo;
    }

    @Override
    public ApiResponse createLoginUserSession(LoginSessionDto loginSessionDto) {
        try {
            LoginSession session = new LoginSession();
            BeanUtils.copyProperties(loginSessionDto, session);
            if (session.getSessionId() == null) {
                session.setSessionId(UUID.randomUUID());
            }
            session.setLoginTime(LocalDateTime.now());
            session.setActive(true);

            LoginSession saved = loginSessionRepo.save(session);

            return ApiResponse.builder()
                    .httpStatus(HttpStatus.CREATED)
                    .success(true)
                    .message("Login session created successfully")
                    .data(saved)
                    .build();
        } catch (Exception e) {
            log.error("Error creating login session", e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating login session");
        }
    }

    @Override
    public ApiResponse getLoginUserSessions(UUID userId) {
        try {
            LoginSession session = loginSessionRepo.findByUserId(userId);
            if (session == null) {
                return ApiResponse.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .success(false)
                        .message("No active session found for userId: " + userId)
                        .build();
            }

            return ApiResponse.builder()
                    .httpStatus(HttpStatus.OK)
                    .success(true)
                    .data(session)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving login session", e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving login session");
        }
    }

    @Override
    public ApiResponse invalidateLoginUserSession(UUID sessionId) {
        try {
            LoginSession session = loginSessionRepo.findById(sessionId).orElse(null);
            if (session == null || !session.isActive()) {
                return ApiResponse.builder()
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .success(false)
                        .message("No active session found to invalidate for sessionId: " + sessionId)
                        .build();
            }

            session.setActive(false);
            session.setLoggedOut(LocalDateTime.now());
            loginSessionRepo.save(session);

            return ApiResponse.builder()
                    .httpStatus(HttpStatus.OK)
                    .success(true)
                    .message("Login session invalidated successfully")
                    .data(session)
                    .build();
        } catch (Exception e) {
            log.error("Error invalidating login session", e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error invalidating login session");
        }
    }

    @Override
    public ApiResponse getListOfActiveSessions() {
        try {
            List<LoginSession> activeSessions = loginSessionRepo.findByIsActiveTrue();
            return ApiResponse.builder()
                    .httpStatus(HttpStatus.OK)
                    .success(true)
                    .data(activeSessions)
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving active login sessions", e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving active login sessions");
        }
    }
}
