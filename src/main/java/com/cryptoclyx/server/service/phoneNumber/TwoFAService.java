package com.cryptoclyx.server.service.phoneNumber;

import com.cryptoclyx.server.config.security.jwt.JWTTokenHelper;
import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.exceptions.VerificationTokenExpiredException;
import com.cryptoclyx.server.exceptions.VerificationTokenNotFoundException;
import com.cryptoclyx.server.payload.req.OtpSendRequest;
import com.cryptoclyx.server.payload.req.OtpVerifyRequest;
import com.cryptoclyx.server.payload.res.AuthTokenResponse;
import com.cryptoclyx.server.repository.UserRepository;
import com.cryptoclyx.server.service.auth.TokenService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Log4j2
@Service
public class TwoFAService {

    @Value("${twilio.accountSid}")
    private String twilioAccountId;

    @Value("${twilio.authToken}")
    private String twilioAuthToken;

    @Value("${twilio.phoneNumber}")
    private String twilioNumber;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;


    @PostConstruct
    public void init() {
        Twilio.init(twilioAccountId, twilioAuthToken);
    }


    public AuthTokenResponse confirmOperationByOtpSend(String userEmail) {
        if(StringUtils.isBlank(userEmail)) {
            throw new UsernameNotFoundException("User email can't be blank");
        }

        User user = userRepository.findByEmail(userEmail);
        String phoneNumber = user.getPhoneNumber();
        String otp = random4DigitsCode();
        log.debug("-----------------Clyx OTP {}-----------------------", otp);
        sendSMS(phoneNumber, otp);

        AuthTokenResponse authTokenResponse = tokenService.generateTokenWithOtp(user, otp);

        return authTokenResponse;
    }


    @Transactional
    public AuthTokenResponse sendOtp(OtpSendRequest req) {

        AuthTokenResponse authToken = tokenService.getVerificationTokenByTokenString(req.getToken());

        if (null == authToken) {
            throw new VerificationTokenNotFoundException("There is no such verification token");
        }

        if (tokenService.isExpired(authToken)) {
            throw new VerificationTokenExpiredException("Verification token has expired");
        }

        //update phone number
        User user = userRepository.findByEmail(authToken.getUser().getEmail());
        user.setPhoneNumber(req.getPhoneNumber());
        userRepository.save(user);

        String otp = random4DigitsCode();
        log.debug("-----------------Clyx OTP {}-----------------------", otp);
        sendSMS(req.getPhoneNumber(), otp);
        AuthTokenResponse authTokenResponse = tokenService.generateTokenWithOtp(user, otp);

        return authTokenResponse;
    }

    @Transactional
    public boolean verifyOtp(OtpVerifyRequest req) {
        AuthTokenResponse authToken = tokenService.getVerificationTokenByTokenString(req.getToken());

        if (null == authToken) {
            throw new VerificationTokenNotFoundException("There is no such verification token");
        }

        if (tokenService.isExpired(authToken)) {
            throw new VerificationTokenExpiredException("Verification token has expired");
        }

        String otp = authToken.getOtp();
        if (StringUtils.isEmpty(otp)) {
            throw new VerificationTokenNotFoundException("There is no such OTP token in db");
        }

        if (!otp.equals(req.getOtp())) {
            throw new VerificationTokenNotFoundException("OTP is incorrect");
        }
        User user = userRepository.findByEmail(authToken.getUser().getEmail());
        user.setIsPhoneNumberVerified(true);
        userRepository.save(user);
        tokenService.invalidateAllTokens(user);

        return true;
    }

    @Async
    private void sendSMS(String phoneNumber, String otp) {
        Message.creator(new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioNumber), "Clyx OTP: " + otp).create();
    }

    private String random4DigitsCode(){
        Random random = new Random();
        int randomNumber = random.nextInt(9000) + 1000;
        return String.format("%04d", randomNumber);
    }
}
