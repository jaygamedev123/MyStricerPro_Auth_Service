package com.striker.auth.service;

import com.striker.auth.dto.ApiResponse;
import com.striker.auth.dto.LoginSessionDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("loginSessionService")
public interface ILoginSessionService {

    ApiResponse createLoginUserSession(LoginSessionDto loginSessionDto);

    ApiResponse getLoginUserSessions(UUID userId);

    ApiResponse invalidateLoginUserSession(UUID sessionId);

    ApiResponse getListOfActiveSessions();
}
