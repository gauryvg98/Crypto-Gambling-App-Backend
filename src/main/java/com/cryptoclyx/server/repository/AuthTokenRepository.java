package com.cryptoclyx.server.repository;


import com.cryptoclyx.server.entity.AuthToken;
import com.cryptoclyx.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    AuthToken findByToken(String token);

    void deleteAllByUser(User user);
}
