### Backend

`mvn clean package -Dspring.profiles.active=prod`

### Admin Wallet Script
`INSERT INTO public.wallets( user_id, network, cluster, public_address, status, created, modified) VALUES (1, 'SOLANA', 'DEVNET', '9BwJhMfhLhZSsQpQ4gxiYqzAD71DZNjn82KjmVyVCZEd', 'ACTIVE', '2024-04-02 15:43:45.703114', '2024-04-02 15:43:45.703114');`

### Wallet Top Up Flow
1. User clicks `/api/payment/top-up/start` button/endpoint - transaction starts. Transaction has expiration time. 
During that time the user should fund money. If time is expired then the cron job #1 will change transaction status to `EXPIRED`
and cron job #2 will not take effect - will not start fund confirmation process (blockchain transaction checks)
2. Once user funded money he clicks `/api/payment/top-up/commit`. We change transaction status to `CONFIRMING_USER_TRANSACTION`
and cron job #2 starts transaction confirmation process (blockchain transaction checks)
3. Cron Job #1 - very simple one, it calls sql query which changes transaction status if time is expired
4. Cron job #2 - a bit more complicated, it checks blockchain transactions, if found then change transaction status to `SUCCESS`
if not found then change transaction status to `FAILED`

### Wallet Withdraw Flow
1. User enters amount of SOL to withdraw and click Withdraw button->`/api/payment/withdraw`. The system checks:
a) if user has enough SOL on his/her balance for withdrawal
b) if there is no active withdrawal transaction (only 1 active transaction is allowed for money/transaction consistency, otherwise we could lose money when user submit 2+ transactions at the same time)
2. System saves transaction record in the db. Later it will be processed by cron job #3
3. Cron job #3 fetches all pending transactions from db (status `WITHDRAW_REQUESTED`), checks if app system wallet has enough balance for transfer
- if so, then transfer SOL to user wallet, extracts appropriate SOL amount from `user.solBalance` and change transaction status to `WITHDRAW_REQ_SENT_TO_NETWORK`
- if no, then send email notification about low system wallet balance
- once the SOL has sent we have to check on chain if the transaction is successful or not. The is a scope of cron job #4
4. Cron job #4 fetches all transactions from the db with status `WITHDRAW_REQ_SENT_TO_NETWORK` and checks on SOLANA chain if transaction is successful
- if so, then change transaction status in db to `SUCCESS`
- if no, then change transaction status to `FAILED`
