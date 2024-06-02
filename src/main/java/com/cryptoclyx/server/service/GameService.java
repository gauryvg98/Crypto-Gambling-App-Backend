package com.cryptoclyx.server.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cryptoclyx.server.entity.Game;
import com.cryptoclyx.server.repository.GameRepository;

import java.util.List;
@Service
public class GameService {
  
    @Autowired
    private GameRepository gameRepository;

    public List<Game> GetGames() {
      // TODO : IMPLEMENT THIS
      return null;
    }

    public Game CreateGame(Long userId) {
      // TODO : IMPLEMENT THIS
    }

}
