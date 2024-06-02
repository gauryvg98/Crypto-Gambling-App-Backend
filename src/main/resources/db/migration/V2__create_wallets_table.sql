CREATE TABLE app_wallets_config (
    id SERIAL PRIMARY KEY,
    network VARCHAR(100),
    cluster VARCHAR(100),
    public_address VARCHAR(255),
    private_wallet_key VARCHAR(255), --encrypted
    status VARCHAR(255),
    created TIMESTAMP,
    modified TIMESTAMP
);


INSERT INTO public.app_wallets_config(id, network, cluster, public_address, private_wallet_key, status, created, modified)
VALUES (1, 'SOLANA', 'DEVNET', '9BwJhMfhLhZSsQpQ4gxiYqzAD71DZNjn82KjmVyVCZEd', 'U0Q3+MbEQ5N8Ve0BMvTf5zk789RVnfZ/LXAiwXav5zgXF8yweC58bubmFVsc/tmEusEYFOGmV4NUFX6EKiaIooUrm755/vWMoyUK24zRCl/nTlt0sqVCNHCmyHNMrio+', 'ACTIVE', '2024-04-02 15:43:45.703114', '2024-04-02 15:43:45.703114');


CREATE TABLE transaction_logs (
    id SERIAL PRIMARY KEY,
    uuid VARCHAR(255) UNIQUE,
    user_id BIGINT NOT NULL,
    operation_type VARCHAR(255),
    network VARCHAR(255),
    cluster VARCHAR(255),
    user_wallet VARCHAR(255),
    transaction_hash VARCHAR(255),
    amount BIGINT,
    status VARCHAR(255),
    failure_count INTEGER,
    message TEXT,
    created TIMESTAMP,
    modified TIMESTAMP,
    CONSTRAINT fk_transaction_logs_user_id FOREIGN KEY (user_id) REFERENCES users(user_id)
);
CREATE INDEX idx_transaction_logs_user_id ON transaction_logs(user_id);