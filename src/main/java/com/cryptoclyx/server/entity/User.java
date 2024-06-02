package com.cryptoclyx.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Entity
@Table(name = "Users")
@Getter @Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "nickname", unique = true, length = 255)
    private String nickname;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "phone_number", unique = true, length = 255)
    private String phoneNumber;

    @Column(name = "password", nullable = false, length = 600)
    private String password;

    @Column(name = "role", nullable = false, length = 100)
    private String role = "PLAYER";

    @Column(name = "level", length = 256)
    private String level = "Bronze";

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Column(name = "is_phone_number_verified")
    private Boolean isPhoneNumberVerified = false;

    @Column(name = "is_2fa_enabled")
    private Boolean is2FaEnabled = true; //2 factor authentication

    @Column(name = "sol_balance")
    private Long solBalance = 0l;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "modified")
    private LocalDateTime modified;

    @PrePersist
    public void prePersist() {
        created = LocalDateTime.now();
        modified = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        modified = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(this.role.split(","))
                .map(String::trim)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.nickname;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEmailVerified;
    }
}
