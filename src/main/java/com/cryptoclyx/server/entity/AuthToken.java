package com.cryptoclyx.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity(name = "auth_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {

    @Transient
    private int TOKEN_EXPIRATION_TIME = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "otp")
    private String otp;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    public AuthToken(String token, User user, String otp) {
        this.token = token;
        this.user = user;
        this.otp = otp;
        this.expirationDate = calculateExpiryDate(TOKEN_EXPIRATION_TIME);
    }

    private Date calculateExpiryDate(int expirationTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expirationTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

}
