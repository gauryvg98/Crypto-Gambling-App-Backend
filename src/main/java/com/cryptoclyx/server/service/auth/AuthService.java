package com.cryptoclyx.server.service.auth;

import com.cryptoclyx.server.config.security.jwt.JWTTokenHelper;
import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.exceptions.EmailException;
import com.cryptoclyx.server.exceptions.PhoneNumberConfirmationRequired;
import com.cryptoclyx.server.payload.req.SigninRequest;
import com.cryptoclyx.server.payload.req.SignupRequest;
import com.cryptoclyx.server.payload.res.AuthTokenResponse;
import com.cryptoclyx.server.payload.res.JwtResponse;
import com.cryptoclyx.server.repository.UserRepository;
import com.cryptoclyx.server.repository.AppWalletConfigRepository;
import com.cryptoclyx.server.utils.EmailUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collections;

@Log4j2
@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JWTTokenHelper jWTTokenHelper;

    private final AppWalletConfigRepository walletRepository;

    private final TokenService authTokenService;


    /**
     * This method logins user in our system.
     * @param req
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public JwtResponse loginUser(SigninRequest req) {
        if(!doesExist(req.getEmail())) {
            log.warn("User with email {} doesn't exist in the system", EmailUtils.maskEmail(req.getEmail()));
            throw new UsernameNotFoundException("Bad credentials");
        }
        User user = userRepository.findByEmail(req.getEmail());
        if(null != user && !user.getIsEmailVerified()) {
            if(passwordEncoder.matches(req.getPassword(), user.getPassword())) { //checking a case when user should confirm email
                throw new EmailException("Please confirm your email");
            } else {
                throw new BadCredentialsException("Bad credentials");
            }
        }
        if(null != user && user.getIs2FaEnabled() && !user.getIsPhoneNumberVerified()) {
            AuthTokenResponse authToken = authTokenService.generateVerificationToken(user);
            throw new PhoneNumberConfirmationRequired("Phone number confirmation is required", authToken);
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (final AuthenticationException e) {
            throw new BadCredentialsException(e.getMessage());
        }
        Pair<String, String> tokenPair = jWTTokenHelper.createTokenPair(req.getEmail(), Collections.emptyList());
        Instant instant = Instant.now().plus(jWTTokenHelper.getExpireInSeconds(), ChronoUnit.SECONDS);
        Calendar expiresAt = Calendar.getInstance();
        expiresAt.setTimeInMillis(instant.toEpochMilli());
        return new JwtResponse(req.getEmail(), tokenPair.getLeft(), tokenPair.getRight(), expiresAt, user.getRole());
    }

    private boolean doesExist(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * This method registers new user in our system.
     * @param req
     */
    public User registerUser(SignupRequest req) throws Exception {

        if (isNicknameExist(req.getNickname())) {
            log.error("User with this nickname already exists.");
            throw new EmailException("User with this nickname already exists.");
        }
        if (isEmailExist(req.getEmail())) {
            log.error("User with this email already exists.");
            throw new EmailException("User with this email already exists.");
        }

        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        return userRepository.save(user);
    }


    public boolean isEmailExist(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public boolean isNicknameExist(String nickname) {
        return userRepository.findByNickname(nickname) != null;
    }

    public User getUserByEmail(String email){
        User byEmail = userRepository.findByEmail(email);
        if (byEmail == null) {
            log.error("User with this email does not exists.");
            throw new EmailException("User with this email does not exists.");
        }
        return byEmail;
    }


    public User saveNewUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
}
