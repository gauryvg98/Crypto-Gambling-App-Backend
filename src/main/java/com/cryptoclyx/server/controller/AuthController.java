package com.cryptoclyx.server.controller;

import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.exceptions.ResponseObject;
import com.cryptoclyx.server.payload.req.ChangePasswordRequest;
import com.cryptoclyx.server.payload.req.SigninRequest;
import com.cryptoclyx.server.payload.req.SignupRequest;
import com.cryptoclyx.server.payload.res.AuthTokenResponse;
import com.cryptoclyx.server.repository.UserRepository;
import com.cryptoclyx.server.service.auth.TokenService;
import com.cryptoclyx.server.service.email.EmailSenderService;
import com.cryptoclyx.server.service.email.EmailVerificationService;
import com.cryptoclyx.server.service.auth.AuthService;
import com.cryptoclyx.server.utils.EmailUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Log4j2
@Validated
@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailSenderService emailService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailVerificationService verificationService;

    @PostMapping("/signup")
    @Operation(summary = "User registration")
    public ResponseEntity register(@RequestBody @Valid SignupRequest signupRequest) throws Exception {

        User user = authService.registerUser(signupRequest);

        log.debug("User {} registered successfully", EmailUtils.maskEmail(signupRequest.getEmail()));

        AuthTokenResponse verificationToken = tokenService.generateVerificationToken(user);
        emailService.sendUserRegistrationMail(user, verificationToken);

        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Signup is success. We sent you an email with an instruction for account activation");

        return ResponseEntity.ok(resObj);
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity login(@RequestBody @Valid SigninRequest signinRequest) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return ResponseEntity.ok(authService.loginUser(signinRequest));
    }

    @GetMapping("/resend/email-confirmation/{email}")
    public ResponseEntity sendConfirmationEmail(@PathVariable String email) {

        log.debug("Resend email confirmation request for email {}", EmailUtils.maskEmail(email));

        User user = authService.getUserByEmail(email);
        AuthTokenResponse verificationToken = tokenService.generateVerificationToken(user);
        emailService.sendUserRegistrationMail(user, verificationToken);

        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Confirmation email has been re-sent");

        return ResponseEntity.ok(resObj);
    }

    @GetMapping("/email-confirm/{token}")
    public ResponseEntity confirmEmail(@PathVariable String token) {

        User user = verificationService.confirmEmail(token);
        //return verification token for OTP verification

        if(user.getIs2FaEnabled() && !user.getIsPhoneNumberVerified()) {
            AuthTokenResponse authTokenResponse = tokenService.generateVerificationToken(user);

            ResponseObject resObj = new ResponseObject();
            resObj.setHttpStatus(HttpStatus.I_AM_A_TEAPOT.value());
            resObj.setMessage("Email is confirmed. Please add and verify a phone number to activate account");
            resObj.setDetails(authTokenResponse);
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(resObj);
        } else {
            ResponseObject resObj = new ResponseObject();
            resObj.setHttpStatus(HttpStatus.OK.value());
            resObj.setMessage("Email is confirmed. Account is activated");

            return ResponseEntity.ok(resObj);
        }

    }

    @GetMapping("/send/reset-password-email/{email}")
    public ResponseEntity<?> sendResetPasswordByEmail(@PathVariable String email) {
        User user = authService.getUserByEmail(email);
        AuthTokenResponse verificationToken = tokenService.generateVerificationToken(user);
        emailService.sendResetPasswordEmail(user, verificationToken);

        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Email has been sent");

        return ResponseEntity.ok(resObj);
    }

    @Transactional
    @PostMapping("/change-password")
    public ResponseEntity<?> resetPasswordByVerificationToken(@RequestBody @Valid ChangePasswordRequest changePassword)
            throws InvalidKeySpecException, NoSuchAlgorithmException {

        User user = verificationService.getUserByVerificationTokenString(changePassword.getVerificationToken());
        User changedUser = authService.saveNewUserPassword(user, changePassword.getNewPassword());
        tokenService.invalidateAllTokens(changedUser);
        emailService.sendPasswordChangedEmail(changedUser);

        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Password changed successfully");

        return ResponseEntity.ok(resObj);

    }
}