package com.cryptoclyx.server.service;

/*import com.cryptoclyx.server.repository.GamesRepository;
import com.cryptoclyx.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;*/
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaderboardService {
   /* @Autowired
    private GamesRepository gamesRepository;
    @Autowired
    private UserRepository userRepository;
    public Game playGame(Long userId) {
        Game game = new Game();
        game.setUserId(userId);
        boolean isWin = new Random().nextBoolean();
        game.setOutcome(isWin ? "WIN" : "LOSS");
        gamesRepository.save(game);
        if (isWin) {
            updateUserLevel(userId);
        }
        return game;
    }
    private void updateUserLevel(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            long winCount = gamesRepository.countByUserIdAndOutcome(userId, "WIN");
            String currentLevel = user.getLevel();
            String newLevel = null;
            if (winCount >= 100 && !"Platinum".equals(currentLevel)) {
                newLevel = "Platinum";
            } else if (winCount >= 50 && !"Gold".equals(currentLevel)) {
                newLevel = "Gold";
            } else if (winCount >= 10 && !"Silver".equals(currentLevel)) {
                newLevel = "Silver";
            }
            if (newLevel != null) {
                user.setLevel(newLevel);
                userRepository.save(user);
            }
        }
    }*/
} 