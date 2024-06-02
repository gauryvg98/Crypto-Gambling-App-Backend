package com.cryptoclyx.server.service.email;


import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.exceptions.VerificationTokenExpiredException;
import com.cryptoclyx.server.exceptions.VerificationTokenNotFoundException;
import com.cryptoclyx.server.payload.res.AuthTokenResponse;
import com.cryptoclyx.server.repository.UserRepository;
import com.cryptoclyx.server.service.auth.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationService {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public User confirmEmail(String token) {

        AuthTokenResponse emailVerification = getEmailConfirmationToken(token);

        if (emailVerification == null) {
            throw new VerificationTokenNotFoundException("There is no such verification token in our system");
        }

        if (tokenService.isExpired(emailVerification)) {
            throw new VerificationTokenExpiredException("Token has expired");
        }

        User user = userRepository.findByEmail(emailVerification.getUser().getEmail());
        user.setIsEmailVerified(true);
        User savedUser = userRepository.save(user);
        tokenService.invalidateAllTokens(user);

        return savedUser;

    }

    private AuthTokenResponse getEmailConfirmationToken(String token) {
        return tokenService.getVerificationTokenByTokenString(token);
    }


    public User getUserByVerificationTokenString(String token) {
        AuthTokenResponse verificationToken = tokenService.getVerificationTokenByTokenString(token);
        if (verificationToken == null) {
            log.error("Verification token {} was not found", token);
            throw new VerificationTokenNotFoundException("Verification token not found " + token);
        }
        if (tokenService.isVerificationNotExpired(verificationToken)) {
            return userRepository.findByEmail(verificationToken.getUser().getEmail());
        }
        return null;
    }

}
