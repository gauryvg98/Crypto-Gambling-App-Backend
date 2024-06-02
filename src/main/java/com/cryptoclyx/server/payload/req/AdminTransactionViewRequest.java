package com.cryptoclyx.server.payload.req;

import com.cryptoclyx.server.entity.enums.CryptoNetwork;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AdminTransactionViewRequest {

    private CryptoNetwork network;
    private TransactionType transactionType;


    public enum TransactionType {
        ALL("ALL"),
        TOP_UP("TOP_UP"),
        WITHDRAW("WITHDRAW");

        final String value;

        TransactionType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


}
