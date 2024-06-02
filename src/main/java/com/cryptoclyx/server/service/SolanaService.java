package com.cryptoclyx.server.service;


import com.cryptoclyx.server.entity.AppWalletConfig;
import com.cryptoclyx.server.entity.enums.WalletStatus;
import com.cryptoclyx.server.payload.AppWalletConfigDto;
import com.cryptoclyx.server.repository.AppWalletConfigRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.Base58;
import org.modelmapper.ModelMapper;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.rpc.types.SignatureStatuses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cryptoclyx.server.entity.enums.CryptoNetwork.SOLANA;

@Log4j2
@Service
public class SolanaService {

    @Autowired
    private AppWalletConfigRepository walletRepository;

    @Autowired
    private ModelMapper modelMapper;

    private RpcClient client;
    private Cluster network;

    public SolanaService(@Value("${crypto.solana.cluster:DEVNET}") Cluster network) {
        this.network = network;
        this.client = new RpcClient(network);
    }

    public Pair<String, byte[]> createWallet() {
        Account account = new Account();
        PublicKey publicKey = account.getPublicKey();
        String publicKeyBase58 = Base58.encode(publicKey.toByteArray());

        return Pair.of(publicKeyBase58, account.getSecretKey());
    }

    public Long getBalance(String walletAddress) throws RpcException {

        return client.getApi().getBalance(new PublicKey(walletAddress));

    }

    public String sendSol(String fromWallet, byte[] fromWalletSecretKey,  String toWallet, long amount) throws RpcException {

        PublicKey fromPublicKey = new PublicKey(fromWallet);
        PublicKey toPublickKey = new PublicKey(toWallet);
        long lamports = amount;

        Account signer = new Account(fromWalletSecretKey);

        Transaction transaction = new Transaction();
        transaction.addInstruction(SystemProgram.transfer(fromPublicKey, toPublickKey, lamports));

        String signature = client.getApi().sendTransaction(transaction, signer);

        return signature;

    }

    /**
     * Returns list of statuses: finalized,
     * @param transactionHash
     * @return
     * @throws RpcException
     */
    public List<String> getTransactionStatus(String transactionHash) throws RpcException {
        SignatureStatuses signatureStatuses = client.getApi().getSignatureStatuses(List.of(transactionHash), true);
        return signatureStatuses.getValue().stream().map(SignatureStatuses.Value::getConfirmationStatus).toList();
    }


    public AppWalletConfigDto getAppWallet() {
        AppWalletConfig wallet = walletRepository.findWallets(SOLANA.name(), this.network.name(), WalletStatus.ACTIVE);
        AppWalletConfigDto solanaAppWallet = modelMapper.map(wallet, AppWalletConfigDto.class);
        return solanaAppWallet;
    }

    public boolean isTransactionConfirmed(String transactionHash) {
        try {
            ConfirmedTransaction transaction = client.getApi().getTransaction(transactionHash);
            return transaction != null; //if null then transaction is not confirmed
        } catch (RpcException e) {
            log.warn("Transaction is not confirmed yet OR smth wrong with the transaction hash");
            return false;
        }
    }

    public boolean isTransactionSuccessful(String transactionHash) {
        try {
            ConfirmedTransaction transaction = client.getApi().getTransaction(transactionHash);
            return transaction != null && transaction.getMeta().getErr() == null;
        } catch (RpcException e) {
            log.warn("Transaction is not confirmed/successfull yet OR smth wrong with the transaction hash");
            return false;
        }
    }
}
