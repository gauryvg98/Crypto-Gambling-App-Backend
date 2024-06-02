package com.cryptoclyx.server.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@CrossOrigin
@RestController
@RequestMapping("/api/game")
public class GameController {


    @GetMapping("/public")
    public ResponseEntity publicEndpoint() {
        return ResponseEntity.ok("public endpoint");
    }

    @GetMapping("/private")
    @PreAuthorize("hasAuthority('PLAYER')")
    public ResponseEntity privateEndpoint() {
        return ResponseEntity.ok("private endpoint");
    }
}