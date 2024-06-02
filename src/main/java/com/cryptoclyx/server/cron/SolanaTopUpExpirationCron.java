package com.cryptoclyx.server.cron;

import com.cryptoclyx.server.entity.TransactionLog;
import com.cryptoclyx.server.entity.enums.TransactionStatus;
import com.cryptoclyx.server.repository.TransactionLogRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.cryptoclyx.server.entity.enums.CryptoNetwork.SOLANA;
import static com.cryptoclyx.server.entity.enums.OperationType.TOP_UP;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Log4j2
@Component
public class SolanaTopUpExpirationCron {

    @Value("${crypto.solana.top-up.expiration-time-minutes:30}")
    private int transactionExpirationTime; //minutes

    @Autowired
    private TransactionLogRepository trLogRepository;

    @Scheduled(cron = "0 0/10 * * * *") // Run every 10 minutes
    public void checkIfUnCommittedTransactionsHaveExpired() {
        List<TransactionLog> uncommittedTransactions = trLogRepository.findUncommittedTransactions(TOP_UP, SOLANA);

        if (isNotEmpty(uncommittedTransactions)) {
            log.debug("Amount of transaction to process is {}", uncommittedTransactions.size());

            for (TransactionLog tr : uncommittedTransactions) {
                if(isExpired(tr)) {
                    tr.setStatus(TransactionStatus.EXPIRED);
                    trLogRepository.save(tr);
                }
            }

        } else {
            log.debug("Amount of transaction to process is 0");
        }
    }

    private boolean isExpired(TransactionLog tr) {
        LocalDateTime created = tr.getCreated();
        LocalDateTime trExpirationTIme = created.plusMinutes(this.transactionExpirationTime);
        LocalDateTime now = LocalDateTime.now();

        return tr.getStatus() != TransactionStatus.USER_TRANSFERS_MONEY || trExpirationTIme.isBefore(now);
    }
}
