package com.striker.auth.dto;

import lombok.Builder;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Builder
public class ApiResponse {
    private HttpStatus httpStatus;
    private String message;
    private Object data;
    private boolean success;
    private String errorMessage;

}
