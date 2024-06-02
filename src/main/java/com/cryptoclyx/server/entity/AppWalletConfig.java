package com.cryptoclyx.server.entity;

import com.cryptoclyx.server.entity.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_wallets_config")
@Getter @Setter
public class AppWalletConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "network", length = 100)
    private String network;

    @Column(name = "cluster", length = 100)
    private String cluster;

    @Column(name = "public_address")
    private String publicAddress;

    @Column(name = "private_wallet_key")
    private String privateWalletKey; //encrypted

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private WalletStatus status;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "modified")
    private LocalDateTime modified;

    @PrePersist
    public void prePersist() {
        created = LocalDateTime.now();
        modified = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        modified = LocalDateTime.now();
    }
}