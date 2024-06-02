
package com.cryptoclyx.server.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@CrossOrigin
@RestController
@RequestMapping("/ping")
public class PingController {

    @GetMapping
    public ResponseEntity ping() {
        return ResponseEntity.ok("pong");
    }
}