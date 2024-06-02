package com.cryptoclyx.server.controller;

import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.exceptions.ResponseObject;
import com.cryptoclyx.server.payload.AppWalletConfigDto;
import com.cryptoclyx.server.payload.req.AdminTransactionViewRequest;
import com.cryptoclyx.server.payload.req.TopUpCommitRequest;
import com.cryptoclyx.server.payload.req.TopUpStartRequest;
import com.cryptoclyx.server.payload.req.WithdrawRequest;
import com.cryptoclyx.server.payload.res.TopUpStartResponse;
import com.cryptoclyx.server.payload.res.TransactionLogResponse;
import com.cryptoclyx.server.service.PaymentService;
import com.cryptoclyx.server.service.SolanaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.p2p.solanaj.rpc.RpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Log4j2
@CrossOrigin
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SolanaService solanaService;

    @PostMapping("/top-up/start")
    @Operation(summary = "User starts top up transaction by specifying network (SOL, BTC) and amount for top up")
    public ResponseEntity topUpStart(UsernamePasswordAuthenticationToken token, @RequestBody @Valid TopUpStartRequest req) {
        String curUserEmail = ((User) token.getPrincipal()).getEmail();
        TopUpStartResponse response = paymentService.startTopUp(curUserEmail, req);
        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Top-up has started");
        resObj.setDetails(response);
        return ResponseEntity.ok(resObj);
    }

    @PostMapping("/top-up/commit")
    @Operation(summary = "Once payment has been made the user should commit transaction and specify his wallet address")
    public ResponseEntity topUpCommit(UsernamePasswordAuthenticationToken token, @RequestBody @Valid TopUpCommitRequest req) {
        String curUserEmail = ((User) token.getPrincipal()).getEmail();
        paymentService.commitTopUp(curUserEmail, req);
        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Top Up is submitted, thank you");
        return ResponseEntity.ok(resObj);
    }

    @GetMapping("/transaction-status/{uuid}")
    public ResponseEntity getTransaction(UsernamePasswordAuthenticationToken token, @PathVariable String uuid) {
        String curUserEmail = ((User) token.getPrincipal()).getEmail();
        TopUpStartResponse response = paymentService.getTransaction(curUserEmail, uuid);
        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Transaction status");
        resObj.setDetails(response);
        return ResponseEntity.ok(resObj);
    }

    @GetMapping("/my-transactions")
    public ResponseEntity getMyTransactions(UsernamePasswordAuthenticationToken token,
                                            @Parameter(description = "Pagination information")
                                            @PageableDefault(size = 20, sort = "created") Pageable pageable) {
        String curUserEmail = ((User) token.getPrincipal()).getEmail();
        Page<TransactionLogResponse> userTransactions = paymentService.getUserTransactions(curUserEmail, pageable);
        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("User transactions page");
        resObj.setDetails(userTransactions);
        return ResponseEntity.ok(resObj);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "User requests a money withdraw by specifying crypto network and amount")
    public ResponseEntity withdraw(UsernamePasswordAuthenticationToken token, @RequestBody @Valid WithdrawRequest req) {
        String curUserEmail = ((User) token.getPrincipal()).getEmail();
        paymentService.withdraw(curUserEmail, req);
        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Withdraw is submitted, thank you");
        return ResponseEntity.ok(resObj);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/transactions")
    @Operation(summary = "ADMIN endpoint. Allows to fetch all users transactions. Parameters: TOP_UP, WITHDRAW, ALL")
    public ResponseEntity getTransactions4Withdraw(@RequestBody @Valid AdminTransactionViewRequest req,
                                                   @Parameter(description = "Pagination information")
                                                   @PageableDefault(size = 20, sort = "modified", direction = DESC)
                                                   Pageable pageable) {
        Page<TransactionLogResponse> allUsersWithdrawalTransactions
                = paymentService.getAllUsersTransactions(req, pageable);
        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Admin transactions page");
        resObj.setDetails(allUsersWithdrawalTransactions);
        return ResponseEntity.ok(resObj);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/solana/wallet/balance")
    @Operation(summary = "ADMIN endpoint. Returns app's SOLANA wallet balance")
    public ResponseEntity getSolWalletBalance() throws RpcException {
        AppWalletConfigDto appWallet = solanaService.getAppWallet();
        appWallet.setPrivateWalletKey(null);
        Long balance = solanaService.getBalance(appWallet.getPublicAddress()); //todo: catch possible exception
        appWallet.setBalance(balance);

        ResponseObject resObj = new ResponseObject();
        resObj.setHttpStatus(HttpStatus.OK.value());
        resObj.setMessage("Admin transactions page");
        resObj.setDetails(appWallet);
        return ResponseEntity.ok(resObj);
    }
}