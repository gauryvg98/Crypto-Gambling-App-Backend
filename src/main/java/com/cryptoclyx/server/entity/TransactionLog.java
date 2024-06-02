package com.cryptoclyx.server.entity;

import com.cryptoclyx.server.entity.enums.OperationType;
import com.cryptoclyx.server.entity.enums.TransactionStatus;
import com.cryptoclyx.server.entity.enums.CryptoNetwork;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;



@Entity
@Table(name = "transaction_logs")
@Getter @Setter
public class TransactionLog { //todo: split this entity on 2, apply postgres partitioning

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true, length = 255)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type")
    private OperationType operationType; //withdraw, top_up

    @Enumerated(EnumType.STRING)
    @Column(name = "network")
    private CryptoNetwork network; //SOLANA, BTC

    @Column(name = "cluster")
    private String cluster;

    @Column(name = "user_wallet")
    private String userWallet; //in case operation type is WITHDRAW

    @Column(name = "transaction_hash")
    private String transactionHash; //transaction id in crypto network

    @Column(name = "amount")
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status; //user_funds, checking_transaction, failed, success

    @Column(name = "failure_count")
    private Integer failureCount = 0;

    @Column(name = "message")
    private String message;

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