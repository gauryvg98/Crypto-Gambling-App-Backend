package com.cryptoclyx.server.payload.req;

import com.cryptoclyx.server.entity.enums.CryptoNetwork;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter @Setter
public class WithdrawRequest {

    private CryptoNetwork network;

    //todo: add it later @Length(message = "Min wallet address length is 5 symbols", min = 5)
    private String walletAddress;

    //todo: add it later @Range(min = 500, max = 2000000000, message = "Amount must be between 5000 and 2 000 000 000")
    private long amount;

    private OtpVerifyRequest otpRequest;

}
