package com.cryptoclyx.server.cron;

import com.cryptoclyx.server.entity.TransactionLog;
import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.entity.enums.TransactionStatus;
import com.cryptoclyx.server.repository.TransactionLogRepository;
import com.cryptoclyx.server.repository.UserRepository;
import com.cryptoclyx.server.service.SolanaService;
import lombok.extern.log4j.Log4j2;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.SignatureStatuses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cryptoclyx.server.entity.enums.CryptoNetwork.SOLANA;
import static com.cryptoclyx.server.entity.enums.TransactionStatus.SUCCESS;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Log4j2
@Component
public class SolanaWithdrawalOnChainConfirmationCron {

    @Autowired
    private TransactionLogRepository trLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolanaService solanaService;

    @Transactional
    @Scheduled(cron = "0 0/1 * * * *") // Run every 1 minutes
    public void checkIfTransactionSuccessfulOnChain() {
        List<TransactionLog> transactions4withdraw = trLogRepository.findWithdrawalOnChainConfirmationNeeded(SOLANA);

        if (isNotEmpty(transactions4withdraw)) {
            log.debug("Amount of withdraw transactions to process is {}", transactions4withdraw.size());
            //get on chain status
            for(TransactionLog tr : transactions4withdraw) {
                boolean isTrConfirmed = solanaService.isTransactionConfirmed(tr.getTransactionHash());
                boolean isTrSuccessful = solanaService.isTransactionSuccessful(tr.getTransactionHash());

                if(isTrConfirmed && isTrSuccessful) {
                    //todo: maybe we should check here if correct amount of SOL has been transfered
                    tr.setStatus(SUCCESS);
                    trLogRepository.save(tr);
                } else {
                    if(tr.getFailureCount()>=3) {
                        tr.setStatus(TransactionStatus.FAILED);
                        tr.setMessage("Can't confirm SOL transaction on-chain after 3 times");
                        log.error("Transaction ID:{} status is {}", tr.getId(), tr.getStatus());
                        revertUserBalance(tr);
                    } else {
                        int count = tr.getFailureCount();
                        ++count;
                        tr.setMessage("Can't confirm SOL transaction on chain");
                        tr.setFailureCount(count);
                        log.warn("Transaction ID:{} increasing its failure count", tr.getId());
                    }
                    trLogRepository.save(tr);
                }

            }


            //if success then change status in db to SUCCESS
            //if in progress then skip
            //if failed then add balance to user's account and change db status to FAILED and add reason
        } else {
            log.debug("Amount of withdraw transaction to process is 0");
        }
    }

    private void revertUserBalance(TransactionLog tr) {
        User user = tr.getUser();
        user.setSolBalance(user.getSolBalance()+tr.getAmount());
        userRepository.save(user);
    }
}
