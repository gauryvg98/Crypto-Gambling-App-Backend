package com.cryptoclyx.server.payload;

import com.cryptoclyx.server.entity.enums.WalletStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter @Setter
public class AppWalletConfigDto {

    private Long id;

    private String network;

    private String cluster;

    private String publicAddress;

    @JsonIgnore
    private String privateWalletKey;

    private Long balance;

    private WalletStatus status;

    private LocalDateTime created;

    private LocalDateTime modified;
}
