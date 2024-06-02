package com.cryptoclyx.server.repository;

import com.cryptoclyx.server.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    // TODO : IMPLEMENT THIS
    // ADD SQL QUERY METHODS HERE to fulfil the requirements
} 