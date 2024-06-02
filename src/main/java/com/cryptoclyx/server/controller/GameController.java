package com.cryptoclyx.server.controller;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cryptoclyx.server.service.GameService;

@Log4j2
@CrossOrigin
@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/health")
    public ResponseEntity publicEndpoint() {
        return ResponseEntity.ok("healthy");
    }


     /**
         * TODO : IMPLEMENT THIS
         * 
         * This API should create a new game. 
         * 
         * and takes in the following params in a post request : 
         * created_by string
         * game_price float
         * game_currency enum
         * game_type enum = COINFLIP
         *
     */ 
    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PLAYER')")
    public ResponseEntity createGame() {
        // TODO : IMPLEMENT THIS
        gameService.CreateGame();
        return ResponseEntity.ok().body(null);
    }

    /**
         * TODO : IMPLEMENT THIS
         * 
         * This API should Return a list of game entities which is to be shown to the user. 
         * The API should be paginated.
         * This API should support filtering on :
         *    i) game type, 
         *   ii) whether user can join the game
         *  iii) games created by the user
         * 
         * What games can a user join : 
         * If user has sufficient balance to join a game (Check balance in User entity)
         * 
     */ 
    @GetMapping("/")
    @PreAuthorize("hasAuthority('PLAYER')")
    public ResponseEntity GetGames() {
        return ResponseEntity.ok().body(gameService.GetGames());
    }
}