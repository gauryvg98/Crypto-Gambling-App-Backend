package com.cryptoclyx.server.service;

import com.cryptoclyx.server.entity.TransactionLog;
import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.entity.AppWalletConfig;
import com.cryptoclyx.server.entity.enums.OperationType;
import com.cryptoclyx.server.entity.enums.TransactionStatus;
import com.cryptoclyx.server.entity.enums.WalletStatus;
import com.cryptoclyx.server.exceptions.*;
import com.cryptoclyx.server.payload.req.AdminTransactionViewRequest;
import com.cryptoclyx.server.payload.req.TopUpCommitRequest;
import com.cryptoclyx.server.payload.req.TopUpStartRequest;
import com.cryptoclyx.server.payload.req.WithdrawRequest;
import com.cryptoclyx.server.payload.res.AuthTokenResponse;
import com.cryptoclyx.server.payload.res.TopUpStartResponse;
import com.cryptoclyx.server.payload.res.TransactionLogResponse;
import com.cryptoclyx.server.repository.TransactionLogRepository;
import com.cryptoclyx.server.repository.UserRepository;
import com.cryptoclyx.server.repository.AppWalletConfigRepository;
import com.cryptoclyx.server.service.auth.TokenService;
import com.cryptoclyx.server.service.phoneNumber.TwoFAService;
import com.cryptoclyx.server.utils.EmailUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.p2p.solanaj.rpc.Cluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static com.cryptoclyx.server.entity.enums.CryptoNetwork.SOLANA;
import static com.cryptoclyx.server.entity.enums.OperationType.TOP_UP;
import static com.cryptoclyx.server.entity.enums.OperationType.WITHDRAW;
import static com.cryptoclyx.server.payload.req.AdminTransactionViewRequest.TransactionType.ALL;
import static java.util.stream.Collectors.toList;

@Log4j2
@Service
public class PaymentService {

    @Value("${crypto.solana.cluster:DEVNET}")
    private Cluster solanaCluster;

    @Value("${crypto.solana.top-up.expiration-time-minutes:30}")
    private int transactionExpirationTime; //minutes

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionLogRepository transactionLogRepository;

    @Autowired
    private AppWalletConfigRepository walletRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TwoFAService twoFAService;

    /**
     * The method creates a record in transaction_log table and return SOL wallet address for user's top up.
     * Cron job runs each 15 minutes abd checks if transaction is expired or no
     * If transaction expired (wasn't top up) then it changes status in transaction_log to EXPIRED
     *
     * @param userEmail
     * @param req
     * @return
     */
    @Transactional
    public TopUpStartResponse startTopUp(String userEmail, TopUpStartRequest req) {

        AppWalletConfig solWallet = getAdminWallet();

        //fetch admin wallet
        if (solWallet == null) {
            log.error("Can't fetch solana wallet. Please check if it exists in wallets table in db");
            throw new WalletException("It is our problem with wallet configuration in the app. Please notify customer support");
        }

        User user = userRepository.findByEmail(userEmail);

        //todo: check if top up is allowed (CI/CD purposes)
        //create transaction record
        TransactionLog tr = createTransaction(user, req);
        transactionLogRepository.save(tr);

        TopUpStartResponse res = new TopUpStartResponse();
        res.setTransactionUuid(tr.getUuid());
        res.setNetwork(SOLANA.name());
        res.setWalletAddress(solWallet.getPublicAddress());
        res.setAmount(req.getAmount());
        res.setStatus(tr.getStatus());
        res.setTransactionExpiresInSeconds(getExpirationSeconds(tr.getCreated()));


        //init time + tr exp time = A
        //now time = B
        //tr._______________.exp time
        //_________.now_________
        return res;
    }

    @Transactional
    public TopUpStartResponse getTransaction(String userEmail, String trUuid) {
        AppWalletConfig solWallet = getAdminWallet();

        //fetch admin wallet
        if (solWallet == null) {
            log.error("Can't fetch solana wallet. Please check if it exists in wallets table in db");
            throw new WalletException("It is our problem with wallet configuration in the app. Please notify customer support");
        }

        User user = userRepository.findByEmail(userEmail);
        TransactionLog tr = transactionLogRepository.findByUserAndUuid(user, trUuid);
        if (null == tr) {
            log.warn("Can't find transaction with uuid: {}", trUuid);
            throw new TransactionLogNotFoundException("Can't find transaction with uuid:" + trUuid);
        }

        TopUpStartResponse res = new TopUpStartResponse();
        res.setTransactionUuid(tr.getUuid());
        res.setNetwork(SOLANA.name());
        res.setWalletAddress(solWallet.getPublicAddress());
        res.setAmount(tr.getAmount());
        res.setStatus(tr.getStatus());
        res.setTransactionExpiresInSeconds(getExpirationSeconds(tr.getCreated()));

        return res;
    }


    private long getExpirationSeconds(LocalDateTime createdDate) {
        Calendar trExpirationTime = getExpirationTime(createdDate, Calendar.MINUTE, this.transactionExpirationTime);
        Calendar now = Calendar.getInstance();
        long timeDifferenceInMillis = trExpirationTime.getTimeInMillis() - now.getTimeInMillis();
        if (timeDifferenceInMillis > 0) {
            return timeDifferenceInMillis / 1000;
        } else {
            return 0;
        }
    }

    /**
     * If user transfered money to our wallet then he/she clicks submit button -> commit top up.
     * Our method checks if transaction exists, not expired
     * If everything is fine then we change transaction status to CONFIRMING_USER_TRANSACTION and cron job starts
     * to check if such transaction exists in the blockchain
     *
     * @param userEmail
     * @param req
     */
    @Transactional
    public void commitTopUp(String userEmail, TopUpCommitRequest req) {
        User user = userRepository.findByEmail(userEmail);
        TransactionLog tr = transactionLogRepository.findByUserAndUuid(user, req.getTransactionUuid());
        if (null == tr) {
            log.warn("There is not transaction with uuid: {} for user: {}", EmailUtils.maskEmail(userEmail), req.getTransactionUuid());
            throw new TransactionLogNotFoundException("There is not transaction with uuid " + req.getTransactionUuid() + " for this user");
        }

        if (tr.getStatus() == TransactionStatus.CONFIRMING_USER_TRANSACTION) {
            log.info("Transaction uuid: {} has been already committed", req.getTransactionUuid());
            throw new TransactionLogEntityAlreadyCommitted("Transaction uuid: " + req.getTransactionUuid() + " has been already committed");
        }

        if (tr.getStatus() == TransactionStatus.EXPIRED) {
            log.warn("Transaction uuid: {} is expired", req.getTransactionUuid());
            throw new TransactionLogEntityExpired("Transaction uuid: " + req.getTransactionUuid() + " is expired");
        }

        if (isExpired(tr)) { // time, status
            tr.setStatus(TransactionStatus.EXPIRED);
            tr.setTransactionHash(req.getNetworkTransactionHash());
            //todo: detach transaction and create a new record to have a transaction history
            transactionLogRepository.save(tr);
            log.warn("Transaction uuid: {} has expired", req.getTransactionUuid());
            throw new TransactionLogEntityExpired("Transaction uuid: " + req.getTransactionUuid() + " has expired");
        }

        tr.setStatus(TransactionStatus.CONFIRMING_USER_TRANSACTION);
        tr.setTransactionHash(req.getNetworkTransactionHash());
        //todo: detach transaction and create a new record to have a transaction history
        transactionLogRepository.save(tr);
    }

    private Calendar getExpirationTime(LocalDateTime date, int timeUnit, int amount) {
        Calendar calendar = dateTimeToCalendar(date);
        calendar.add(timeUnit, amount);
        return calendar;
    }

    private Calendar dateTimeToCalendar(LocalDateTime date) {
        ZonedDateTime zonedDateTime = date.atZone(ZoneId.systemDefault());
        Instant instant = zonedDateTime.toInstant();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(instant.toEpochMilli());
        return calendar;
    }

    private boolean isExpired(TransactionLog tr) {
        LocalDateTime created = tr.getCreated();
        LocalDateTime trExpirationTIme = created.plusMinutes(this.transactionExpirationTime);
        LocalDateTime now = LocalDateTime.now();

        return tr.getStatus() != TransactionStatus.USER_TRANSFERS_MONEY || trExpirationTIme.isBefore(now);
    }

    private AppWalletConfig getAdminWallet() {
        return walletRepository.findWallets(
                SOLANA.name(),
                Cluster.DEVNET.name(),
                WalletStatus.ACTIVE);
    }

    private TransactionLog createTransaction(User user, TopUpStartRequest req) {
        TransactionLog tr = new TransactionLog();
        tr.setUuid(UUID.randomUUID().toString());
        tr.setUser(user);
        tr.setOperationType(TOP_UP);
        tr.setNetwork(req.getNetwork());
        tr.setCluster(solanaCluster.name());
        tr.setAmount(req.getAmount());
        tr.setStatus(TransactionStatus.USER_TRANSFERS_MONEY);
        return tr;
    }

    private TransactionLog createTransaction(User user, WithdrawRequest req) {
        TransactionLog tr = new TransactionLog();
        tr.setUuid(UUID.randomUUID().toString());
        tr.setUser(user);
        tr.setOperationType(OperationType.WITHDRAW);
        tr.setNetwork(req.getNetwork());
        tr.setCluster(solanaCluster.name());
        tr.setUserWallet(req.getWalletAddress());
        tr.setAmount(req.getAmount());
        tr.setStatus(TransactionStatus.WITHDRAW_REQUESTED);
        return tr;
    }

    public Page<TransactionLogResponse> getUserTransactions(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email);

        Page<TransactionLog> page = transactionLogRepository.findByUser(user, pageable);
        List<TransactionLogResponse> dtoList = page.stream().map(p -> modelMapper.map(p, TransactionLogResponse.class))
                .collect(toList());

        return new PageImpl<>(dtoList, page.getPageable(), page.getTotalElements());
    }

    public Page<TransactionLogResponse> getAllUsersTransactions(AdminTransactionViewRequest req, Pageable pageable) {

        if(null == req) {
            log.error("Payload can't be null");
            throw new IllegalArgumentException("Payload can't be null");
        }
        Page<TransactionLog> page = null;

        if (null == req.getTransactionType() || null == req.getNetwork()) {
            log.error("Transaction type or network can't be null or empty. Types are: ALL, TOP_UP, WITHDRAW. Networks are: SOL, ETH");
            throw new IllegalArgumentException("Transaction type or network can't be null or empty. Types are: ALL, TOP_UP, WITHDRAW. Networks are: SOL, ETH");

        } else if (req.getTransactionType() == ALL) {
            page = transactionLogRepository.findAll(pageable);

        }else if (req.getTransactionType() == AdminTransactionViewRequest.TransactionType.TOP_UP) {
            page = transactionLogRepository.findByOperationTypeAndNetwork(TOP_UP, req.getNetwork(), pageable);

        } else if (req.getTransactionType() == AdminTransactionViewRequest.TransactionType.WITHDRAW) {
            page = transactionLogRepository.findByOperationTypeAndNetwork(WITHDRAW, req.getNetwork(), pageable);
        }

        List<TransactionLogResponse> dtoList = page.stream().map(p -> modelMapper.map(p, TransactionLogResponse.class))
                .collect(toList());

        return new PageImpl<>(dtoList, page.getPageable(), page.getTotalElements());
    }


    /**
     * User requests money withdraw by specifying network/wallet and amount.
     * We create a record in db and then a cron job will process withdrawal.
     *
     * @param userEmail
     * @param req
     */
    public void withdraw(String userEmail, WithdrawRequest req) {
        User user = userRepository.findByEmail(userEmail);
        if(req.getAmount() > user.getSolBalance()) {
            log.warn("You don't have such amount at your account for withdrawal");
            throw new IllegalArgumentException("You don't have such amount at your account for withdrawal");
        }
        List<TransactionLog> pendingTransactions = transactionLogRepository.findUserTransactionsForWithdrawal(SOLANA, user);
        if(pendingTransactions != null && !pendingTransactions.isEmpty()) {
            log.warn("We are still processing your previous withdrawal request. Please wait once it is completed");
            throw new TemporaryWithdrawalUnavailableException("We are still processing your previous withdrawal request. Please wait once it is completed");
        }
        if(user.getIs2FaEnabled() && (req.getOtpRequest() == null || StringUtils.isBlank(req.getOtpRequest().getOtp()))) {
            AuthTokenResponse authToken = tokenService.generateVerificationToken(user);
            throw new PhoneNumberConfirmationRequired("OTP verification is required", authToken);
        }

        if(user.getIs2FaEnabled()) {
            boolean isOtpVerified = twoFAService.verifyOtp(req.getOtpRequest());
            if(!isOtpVerified) {
                throw new PhoneNumberConfirmationRequired("OTP verification failed");
            }
        }

        TransactionLog transaction = createTransaction(user, req);
        transactionLogRepository.save(transaction);
    }
}
