package com.cryptoclyx.server.service;

import com.cryptoclyx.server.utils.WalletUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.modelmapper.internal.util.Assert;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.rpc.types.LargeAccount;
import org.p2p.solanaj.rpc.types.SignatureStatuses;
import org.springframework.security.core.parameters.P;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolanaServiceTest {

    String publicKeyFrom = "fHb85V7D8MxGpmrVDmyN2E6Y8VdQC6JjwusoPacvTvp";
    String publicKeyTo = "9BwJhMfhLhZSsQpQ4gxiYqzAD71DZNjn82KjmVyVCZEd";
    String privateKeyStrFrom = "bUoQz+mXRxkUvsxrmLKsekGx9//SVjzW2tjgjDQ0F4MJztPD4JCReAIXj1CApFHRG5VCbDP9FaDVqInA0op9cQ==";
    String privateKeyStrTo = "YTo3ugp0JAzmCQJfh0bUzx0dlOsdIKsydR1M0cGE9HJ5qrTbeL9R8pSzznFAKRVBFYPVzG7AX0TDUfAbn5SXDg==";

    //@Test
    public void testCreateWalletAndAirdrop() throws RpcException, InterruptedException {

        //make airdrop
        RpcClient rpcClient = new RpcClient(Cluster.DEVNET);
        //String transactionId = rpcClient.getApi().requestAirdrop(new PublicKey(publicKeyFrom), 2_000_000_000l);
        //System.out.println("Transaction id: "+transactionId);
        //Thread.sleep(15000l);


        //send SOL to send wallet
        SolanaService service = new SolanaService(Cluster.DEVNET);
        String signature = service.sendSol(publicKeyFrom, base64Decode(privateKeyStrFrom), publicKeyTo, 1_000_000_000);

        System.out.println("Transaction signature: "+signature);
        System.out.println("Wallet from balance: "+rpcClient.getApi().getBalance(new PublicKey(publicKeyFrom)));
        System.out.println("Wallet to balance: "+rpcClient.getApi().getBalance(new PublicKey(publicKeyTo)));
        //check transaction

        /*ConfirmedTransaction transaction = rpcClient.getApi().getTransaction(signature);
        System.out.println();
        //check transaction is success
        SignatureStatuses signatureStatuses = rpcClient.getApi().getSignatureStatuses(List.of(signature), true);
        boolean isFinalized = signatureStatuses.getValue().stream().anyMatch(v -> "finalized".equals(v.getConfirmationStatus()));
        if(isFinalized && transaction != null) {
            long trSolTransferAmount = transaction.getMeta().getPostBalances().get(1) - transaction.getMeta().getPreBalances().get(1);
            System.out.println("We successfully transferred "+trSolTransferAmount+" lamports to "+publicKeyTo+ "address");
            //check balance in transaction is the as balance in tr record in db
        }*/
        //check balance
    }

    //@Test
    public void checkTransactionStatus() throws RpcException {
        String errorTransaction = "odkZowKpk4PWCgdYnEktgDTxvSveoCgZqPERQtahGbjnM9NHFwiVKpYRzvLRJLU5BjULjN4GZ6cFLxCA9xv1qEf";
        String successTransaction = "3V8XEU3DhJJ7aXRqfJAizXtUeVWUGSzFLEKoDwjdeBYpdys7ryPGeSxgLr6dnQnM9FPgf6s98EPtncqCmfyNNVzV";

        RpcClient rpcClient = new RpcClient(Cluster.DEVNET);
        ConfirmedTransaction transactionError = rpcClient.getApi().getTransaction(errorTransaction);
        ConfirmedTransaction transactionSuccess = rpcClient.getApi().getTransaction(successTransaction);
        transactionSuccess.getTransaction().getMessage();
        System.out.println();
    }

    //@Test
    public void testWalletEncryption() throws Exception {
        String key = privateKeyStrTo;

        String encryptedStr = WalletUtils.encrypt(key, "solanaEcnryptionKey123@!");
        String decryptedStr = WalletUtils.decrypt(encryptedStr, "solanaEcnryptionKey123@!");
        byte[] secretKeyBytes = base64Decode(key);

        System.out.println("Init key:"+key);
        System.out.println("Encrypted key:"+encryptedStr);

        System.out.println("Decrypted string: " + decryptedStr);
        assertEquals(key, decryptedStr);


    }

    public static void createWallets() {
        Account accountFrom = new Account();
        PublicKey publicKeyFrom = accountFrom.getPublicKey();
        String privateKeyStrFrom = base64Encode(accountFrom.getSecretKey());

        Account accountTo = new Account();
        PublicKey publicKeyTo = accountTo.getPublicKey();
        String privateKeyStrTo = base64Encode(accountTo.getSecretKey());

        System.out.println();
        //System.out.println("Wallet from, public address:"+walletFrom.getLeft()+" private key:"+new String(walletFrom.getRight(), StandardCharsets.UTF_8));
        //System.out.println("Wallet to, public address:"+walletTo.getLeft()+" private key:"+new String(walletTo.getRight(), StandardCharsets.UTF_8));
    }
    // Method to encode byte[] to Base64 String

    public static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    // Method to decode Base64 String to byte[]
    public static byte[] base64Decode(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }
}
