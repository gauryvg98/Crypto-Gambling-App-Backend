package com.cryptoclyx.server.repository;

import com.cryptoclyx.server.entity.TransactionLog;
import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.entity.enums.CryptoNetwork;
import com.cryptoclyx.server.entity.enums.OperationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

    TransactionLog findByUserAndUuid(User user, String transactionUuid);

    @Query("SELECT tr FROM TransactionLog tr WHERE tr.operationType=:operationType " +
            "and tr.network=:cryptoNetwork and tr.status='CONFIRMING_USER_TRANSACTION'")
    List<TransactionLog> findTransactionsForConfirmation(OperationType operationType, CryptoNetwork cryptoNetwork);

    @Query("SELECT tr FROM TransactionLog tr WHERE tr.operationType=:operationType " +
            "and tr.network=:cryptoNetwork and tr.status='USER_TRANSFERS_MONEY'")
    List<TransactionLog> findUncommittedTransactions(OperationType operationType, CryptoNetwork cryptoNetwork);

    @Query("SELECT tr FROM TransactionLog tr WHERE tr.operationType='WITHDRAW' " +
            "and tr.network=:cryptoNetwork and tr.status='WITHDRAW_REQUESTED'")
    List<TransactionLog> findTransactionsForWithdrawal(CryptoNetwork cryptoNetwork);

    @Query("SELECT tr FROM TransactionLog tr WHERE tr.operationType='WITHDRAW' " +
            "and tr.network=:cryptoNetwork and tr.user=:user and tr.status='WITHDRAW_REQUESTED' " +
            "or tr.status=WITHDRAW_REQ_SENT_TO_NETWORK")
    List<TransactionLog> findUserTransactionsForWithdrawal(CryptoNetwork cryptoNetwork, User user);

    @Query("SELECT tr FROM TransactionLog tr WHERE tr.operationType='WITHDRAW' " +
            "and tr.network=:cryptoNetwork and tr.status='WITHDRAW_REQ_SENT_TO_NETWORK'")
    List<TransactionLog> findWithdrawalOnChainConfirmationNeeded(CryptoNetwork cryptoNetwork);

    Page<TransactionLog> findByUser(User user, Pageable pageable);

    Page<TransactionLog> findByOperationTypeAndNetwork(OperationType operationType, CryptoNetwork network, Pageable pageable);
}