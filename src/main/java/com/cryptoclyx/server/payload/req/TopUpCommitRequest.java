package com.cryptoclyx.server.payload.req;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TopUpCommitRequest {

    private String transactionUuid;
    private String networkTransactionHash;
}
