package com.striker.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/")
@RestController
public class WellComeController {

    @GetMapping
    public ResponseEntity<String> greetings() {
        log.info("Inside WellComeController of Auth Service");
        return ResponseEntity.ok("Response From welcome controller of Auth Service Service")
                ;
    }
}
