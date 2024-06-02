package com.cryptoclyx.server.repository;

import com.cryptoclyx.server.entity.AppWalletConfig;
import com.cryptoclyx.server.entity.enums.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AppWalletConfigRepository extends JpaRepository<AppWalletConfig, Long> {


    @Query("SELECT w FROM AppWalletConfig w WHERE w.network=:network and w.cluster=:cluster and w.status=:status")
    AppWalletConfig findWallets(String network, String cluster, WalletStatus status);
}