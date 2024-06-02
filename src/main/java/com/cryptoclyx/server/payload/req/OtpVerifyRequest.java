package com.cryptoclyx.server.payload.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter @Setter
public class OtpVerifyRequest {

    @NotBlank(message = "token should not be blank")
    private String token;

    @NotBlank(message = "Otp should not be blank")
    @Length(message = "Otp length should be 4 symbols", min = 4, max = 4)
    private String otp;
}
