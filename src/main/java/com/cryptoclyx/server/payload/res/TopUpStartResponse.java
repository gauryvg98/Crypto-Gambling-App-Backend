package com.cryptoclyx.server.payload.res;

import com.cryptoclyx.server.entity.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopUpStartResponse {

    private String transactionUuid;
    private String walletAddress;
    private String network;
    private long amount;
    private TransactionStatus status;
    private long transactionExpiresInSeconds;
}
