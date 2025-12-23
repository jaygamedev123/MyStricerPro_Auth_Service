package com.striker.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");

        return ResponseEntity.status(
                        statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value()
                )
                .body(Map.of(
                        "status", statusCode,
                        "message", "Unexpected error occurred"
                ));
    }
}
