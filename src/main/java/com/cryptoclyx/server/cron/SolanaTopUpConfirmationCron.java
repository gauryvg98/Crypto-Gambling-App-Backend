package com.cryptoclyx.server.cron;

import com.cryptoclyx.server.entity.TransactionLog;
import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.entity.enums.TransactionStatus;
import com.cryptoclyx.server.repository.TransactionLogRepository;
import com.cryptoclyx.server.repository.UserRepository;
import com.cryptoclyx.server.repository.AppWalletConfigRepository;
import lombok.extern.log4j.Log4j2;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.cryptoclyx.server.entity.enums.CryptoNetwork.SOLANA;
import static com.cryptoclyx.server.entity.enums.OperationType.TOP_UP;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Log4j2
@Component
public class SolanaTopUpConfirmationCron {

    @Autowired
    private RpcClient rpcClient;

    @Autowired
    private AppWalletConfigRepository walletRepository;

    @Autowired
    private TransactionLogRepository trLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0/5 * * * *") // Run every 5 minutes
    public void checkIfTransactionIsPassed() throws RpcException {

        log.info("Scheduled task start...");

        List<TransactionLog> transactionsForConfirmation = trLogRepository.findTransactionsForConfirmation(TOP_UP, SOLANA);
        if (isNotEmpty(transactionsForConfirmation)) {

            log.info("Amount of transaction to process is {}", transactionsForConfirmation.size());

            for (TransactionLog tr : transactionsForConfirmation) {
                Long dbTransactionLamports = tr.getAmount();
                String transactionHash = tr.getTransactionHash();

                ConfirmedTransaction transaction = null;
                try {
                    transaction = rpcClient.getApi().getTransaction(transactionHash);
                } catch (RpcException e) {
                    log.error("Error checking SOL transaction, reason: {}", e.getMessage());
                }

                if (transaction != null) {
                    long networkTransactionLamports = transaction.getMeta().getPostBalances().get(1) - transaction.getMeta().getPreBalances().get(1);

                    log.debug("Transaction ID: {}, amount of db lamports: {}, amount of transaction network lamports: {}", tr.getId(), dbTransactionLamports, networkTransactionLamports);

                    if (dbTransactionLamports == networkTransactionLamports) {
                        tr.setStatus(TransactionStatus.SUCCESS);
                        User user = tr.getUser();
                        if(isNotNull(user.getSolBalance())) {
                            Long curBalance = user.getSolBalance();
                            curBalance = curBalance + networkTransactionLamports;
                            user.setSolBalance(curBalance);
                        } else {
                            user.setSolBalance(networkTransactionLamports);
                        }
                        userRepository.save(user);
                        log.debug("Transaction ID:{} status is {}", tr.getId(), tr.getStatus());
                    } else {
                       tr.setStatus(TransactionStatus.INCORRECT_TRANSFER_BALANCE);
                       tr.setMessage("Db record balanse is: "+dbTransactionLamports+" lamports, network transaction lamports is: "+networkTransactionLamports);
                    } log.debug("Transaction ID:{} status is {}", tr.getId(), tr.getStatus());
                } else {
                    if(tr.getFailureCount()>=3) {
                        tr.setStatus(TransactionStatus.FAILED);
                        tr.setMessage("Can't confirm transaction after 3 times");
                        log.debug("Transaction ID:{} status is {}", tr.getId(), tr.getStatus());
                    } else {
                        int count = tr.getFailureCount();
                        ++count;
                        tr.setFailureCount(count);
                        log.debug("Transaction ID:{} increasing its failure count", tr.getId());
                    }
                }

                trLogRepository.save(tr);
            }
        } else {
            log.info("Amount of transaction to process is 0");
        }

        log.info("Scheduled task end...");
    }

    private boolean isNotNull(Long solBalance) {
        return solBalance != null && solBalance > 0;
    }
}