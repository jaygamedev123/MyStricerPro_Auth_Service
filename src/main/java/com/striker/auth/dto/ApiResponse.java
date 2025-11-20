package com.striker.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
public class ApiResponse {

    private HttpStatus httpStatus;
    private String message;
    private Object data;
    private boolean success;
    private String errorMessage;

    public static ApiResponse success(Object data) {
        return ApiResponse.builder()
                .httpStatus(HttpStatus.OK)
                .success(true)
                .data(data)
                .build();
    }

    public static ApiResponse error(HttpStatus status, String errorMessage) {
        return ApiResponse.builder()
                .httpStatus(status)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
