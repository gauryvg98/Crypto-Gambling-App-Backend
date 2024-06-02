package com.cryptoclyx.server.controller;

import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.exceptions.ResponseObject;
import com.cryptoclyx.server.payload.req.OtpSendRequest;
import com.cryptoclyx.server.payload.req.OtpVerifyRequest;
import com.cryptoclyx.server.payload.res.AuthTokenResponse;
import com.cryptoclyx.server.service.phoneNumber.TwoFAService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/2fa")
public class TwoFAController {

    @Value("${twilio.accountSid}")
    private String twilioAccountId;

    @Value("${twilio.authToken}")
    private String twilioAuthToken;

    @Value("${twilio.phoneNumber}")
    private String twilioNumber;

    @Autowired
    private TwoFAService twoFAService;

    @GetMapping("/otp/send-test")
    @Operation(summary = "Used non logged in users. When user should confirm his number upon registration process")
    public ResponseEntity sendTestOtp() {
        Twilio.init(twilioAccountId, twilioAuthToken);
        Message.creator(new PhoneNumber("+17653284771"),
                new PhoneNumber(twilioNumber), "Clyx OTP: 1234").create();
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/otp/send-confirmation-code")
    @Operation(summary = "Use this endpoint if user is logged in and you need to confirm operation by sending OTP")
    public ResponseEntity sendOtp(UsernamePasswordAuthenticationToken token) {
        String curUserEmail = ((User) token.getPrincipal()).getEmail();
        AuthTokenResponse res = twoFAService.confirmOperationByOtpSend(curUserEmail);

        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("SMS to "+res.getUser().getPhoneNumber()+" has been sent");
        resObj.setDetails(res);
        return ResponseEntity.ok(resObj);
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Used non logged in users. When user should confirm his number upon registration process")
    public ResponseEntity sendOtp(@RequestBody @Valid OtpSendRequest req) {
        AuthTokenResponse authTokenResponse = twoFAService.sendOtp(req);

        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("SMS to "+req.getPhoneNumber()+" has been sent");
        resObj.setDetails(authTokenResponse);
        return ResponseEntity.ok(resObj);
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Used non logged in users. When user should confirm his number upon registration process")
    public ResponseEntity verifyOtp(@RequestBody @Valid OtpVerifyRequest req) {
        twoFAService.verifyOtp(req);
        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Phone number has been verified");
        return ResponseEntity.ok(resObj);
    }
}