package com.cryptoclyx.server.cron;

import com.cryptoclyx.server.entity.TransactionLog;
import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.entity.enums.TransactionStatus;
import com.cryptoclyx.server.payload.AppWalletConfigDto;
import com.cryptoclyx.server.payload.res.UserProfileResponse;
import com.cryptoclyx.server.repository.AppWalletConfigRepository;
import com.cryptoclyx.server.repository.TransactionLogRepository;
import com.cryptoclyx.server.repository.UserRepository;
import com.cryptoclyx.server.service.SolanaService;
import com.cryptoclyx.server.service.auth.UserService;
import com.cryptoclyx.server.service.email.EmailSenderService;
import com.cryptoclyx.server.utils.WalletUtils;
import lombok.extern.log4j.Log4j2;
import org.p2p.solanaj.rpc.RpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;

import static com.cryptoclyx.server.entity.enums.CryptoNetwork.SOLANA;
import static com.cryptoclyx.server.entity.enums.TransactionStatus.WITHDRAW_REQ_SENT_TO_NETWORK;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Log4j2
@Component
public class SolanaWithdrawalCron {

    @Autowired
    private TransactionLogRepository trLogRepository;

    @Autowired
    private SolanaService solanaService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailSenderService emailService;

    @Autowired
    private AppWalletConfigRepository appWalletConfigRepository;

    @Value("${crypto.solana.encryption_key:solanaEcnryptionKey123}")
    private String solanaEncryptionSecretKey;

    private Long solanaTransactionFee = 5000L; //lamports

    @Transactional
    @Scheduled(cron = "0 0/1 * * * *") // Run every 1 minutes
    public void sendSolToUserWallet() {
        List<TransactionLog> transactions4withdraw = trLogRepository.findTransactionsForWithdrawal(SOLANA);

        if (isNotEmpty(transactions4withdraw)) {
            log.debug("Amount of withdraw transactions to process is {}", transactions4withdraw.size());

            for (TransactionLog tr : transactions4withdraw) {
                Long amount2Withdraw = tr.getAmount();
                String userWallet = tr.getUserWallet();
                AppWalletConfigDto appWallet = solanaService.getAppWallet();
                try {
                    Long appWalletBalance = solanaService.getBalance(appWallet.getPublicAddress());
                    if (appWalletBalance > 0 && amount2Withdraw < appWalletBalance - solanaTransactionFee) {

                        String transactionHash = doWithdraw(appWallet, userWallet, tr);

                        updateUserAccount(tr);

                        tr.setTransactionHash(transactionHash);
                        tr.setStatus(WITHDRAW_REQ_SENT_TO_NETWORK);
                        tr.setFailureCount(0); //reset counter, since it might be needed for on chain confirmation job
                        tr.setMessage("");
                        trLogRepository.save(tr);
                    } else {
                        //if smth is worng with admin wallet balance then send email notification
                        List<String> adminEmails = userService.getAdmins()
                                .stream()
                                .map(UserProfileResponse::getEmail)
                                .toList();

                        adminEmails.forEach(adminEmail -> {
                            emailService.sendAppWalletLowBalanceEmail(adminEmail, "SOLANA", appWallet.getPublicAddress(), appWalletBalance);
                        });
                    }
                } catch (Exception e) {
                    log.error("Solana NETWORK CONECTION EXCEPTION", e);
                    if(tr.getFailureCount()>=3) {
                        tr.setStatus(TransactionStatus.FAILED);
                        tr.setMessage("Can't send SOL to a user after 3 times. Error:"+e.getMessage());
                        log.error("Transaction ID:{} status is {}", tr.getId(), tr.getStatus());
                    } else {
                        int count = tr.getFailureCount();
                        ++count;
                        tr.setMessage("Can't send SOL to a user. Error:"+e.getMessage());
                        tr.setFailureCount(count);
                        log.warn("Transaction ID:{} increasing its failure count", tr.getId());
                    }
                    trLogRepository.save(tr);
                }

            }

        } else {
            log.debug("Amount of withdraw transaction to process is 0");
        }
    }


    private String doWithdraw(AppWalletConfigDto appWallet, String userWallet, TransactionLog tr) throws RpcException {
        String privateKeyDecrypted = WalletUtils.decrypt(appWallet.getPrivateWalletKey(), solanaEncryptionSecretKey);
        byte[] privateKeyBytes = base64Decode(privateKeyDecrypted);
        return solanaService.sendSol(
                appWallet.getPublicAddress(),
                privateKeyBytes,
                userWallet,
                tr.getAmount());
        //todo: we should check sol network if transaction is success and if so mark it as completed
        //todo: if no then log error and notify admin AND add balance back to user.balance
    }

    private void updateUserAccount(TransactionLog tr) {
        User user = tr.getUser();
        updateUserBalance(user, tr.getAmount());
        userRepository.save(user);
    }

    private void updateUserBalance(User user, Long amountWithdrawn) {
        Long newUserSolBalance = user.getSolBalance() - amountWithdrawn;
        newUserSolBalance = newUserSolBalance < 0 ? 0 : newUserSolBalance;
        user.setSolBalance(newUserSolBalance);
    }

    private byte[] base64Decode(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }
}
