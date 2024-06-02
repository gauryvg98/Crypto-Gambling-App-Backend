package com.cryptoclyx.server.payload.res;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class UserProfileResponse {

    private Long id;

    private String nickname;

    private String firstName;

    private String lastName;

    private String email;

    private String role = "PLAYER";

    private String level = "Bronze";

    private long solBalance;

    private String phoneNumber;

    private Boolean isEmailVerified;

    private Boolean isPhoneNumberVerified;

    private Boolean is2FaEnabled;

    private LocalDateTime created;

    private LocalDateTime modified;

}
