package com.cryptoclyx.server.service.auth;

import com.cryptoclyx.server.entity.AuthToken;
import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.exceptions.VerificationTokenExpiredException;
import com.cryptoclyx.server.payload.res.AuthTokenResponse;
import com.cryptoclyx.server.repository.AuthTokenRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Log4j2
@Service
@Transactional
@AllArgsConstructor
public class TokenService {

    private final AuthTokenRepository tokenRepository;
    private final ModelMapper modelMapper;

    public AuthTokenResponse generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        return createVerificationToken(user, token, null);
    }

    public AuthTokenResponse generateTokenWithOtp(User user, String otp) {
        String token = UUID.randomUUID().toString();
        return createVerificationToken(user, token, otp);
    }

    public AuthTokenResponse createVerificationToken(User user, String token, String otp) {
        AuthToken myToken = new AuthToken(token, user, otp);
        AuthToken saved = tokenRepository.save(myToken);
        return modelMapper.map(saved, AuthTokenResponse.class);
    }

    public AuthTokenResponse getVerificationTokenByTokenString(String token) {
        AuthToken tokenEntity = tokenRepository.findByToken(token);
        if(null == tokenEntity)
            return null;
        return modelMapper.map(tokenEntity, AuthTokenResponse.class);
    }

    public void invalidateAllTokens(User user) {
        tokenRepository.deleteAllByUser(user);
    }

    public boolean isExpired(AuthTokenResponse emailVerification) {
        return emailVerification.getExpirationDate().before(new Date());
    }

    public boolean isVerificationNotExpired(AuthTokenResponse emailVerification) {
        Calendar cal = Calendar.getInstance();
        if (isExpired(emailVerification)) {
            throwExpiredVerifToken(emailVerification);
        }

        return true;
    }

    public void throwExpiredVerifToken(AuthTokenResponse emailVerification) {
        log.error("Verification token {} is expired", emailVerification.getToken());
        throw new VerificationTokenExpiredException("Verification token " + emailVerification.getToken() + " is expired");
    }
}