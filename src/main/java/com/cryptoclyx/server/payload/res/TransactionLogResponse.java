package com.cryptoclyx.server.payload.res;

import com.cryptoclyx.server.entity.enums.CryptoNetwork;
import com.cryptoclyx.server.entity.enums.OperationType;
import com.cryptoclyx.server.entity.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionLogResponse {

    private String uuid;

    private OperationType operationType; //withdraw, top_up

    private CryptoNetwork network; //SOLANA, BTC

    private String cluster;

    private String transactionHash; //transaction id in crypto network

    private Long amount;

    private TransactionStatus status; //user_funds, checking_transaction, failed, success

    private String message;

    private LocalDateTime created;

    private LocalDateTime modified;

}
