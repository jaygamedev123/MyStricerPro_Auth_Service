package com.striker.auth.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionController {
    public ExceptionController() {
    }
    public String handleException(Exception ex) {
        return ex.getMessage();
    }

}
