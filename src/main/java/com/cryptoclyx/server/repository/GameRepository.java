package com.cryptoclyx.server.repository;

import com.cryptoclyx.server.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {

} 