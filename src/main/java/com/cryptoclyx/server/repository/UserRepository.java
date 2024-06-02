package com.cryptoclyx.server.repository;

import com.cryptoclyx.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByNickname(String nickname);

    User findByRole(String role);

    @Query("SELECT u FROM User u WHERE UPPER(u.role) LIKE '%ADMIN%'")
    List<User> getAdmins();

    boolean existsByEmail(String email);
}