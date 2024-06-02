package com.cryptoclyx.server.payload.req;

import com.cryptoclyx.server.entity.enums.CryptoNetwork;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TopUpStartRequest {

    private CryptoNetwork network;
    private long amount;
}
