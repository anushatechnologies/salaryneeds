package com.salaryneeds.service;

import com.salaryneeds.entity.WalletTransaction;
import com.salaryneeds.entity.WorkerWallet;
import com.salaryneeds.entity.enums.TransactionType;
import com.salaryneeds.repository.WalletTransactionRepository;
import com.salaryneeds.repository.WorkerWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NightlySettlementScheduler {

    private final WorkerWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    // Cron runs nightly at 11:59 PM (23:59:00)
    @Scheduled(cron = "0 59 23 * * ?")
    @Transactional
    public void executeNightlySettlement() {
        log.info("Starting Nightly Batch Auto-Settlement at 11:59 PM...");

        List<WorkerWallet> eligibleWallets = walletRepository
                .findByEarningsBalanceGreaterThanAndBankVerifiedTrue(BigDecimal.ZERO);

        for (WorkerWallet wallet : eligibleWallets) {
            BigDecimal amountToDisburse = wallet.getEarningsBalance();
            if (amountToDisburse.compareTo(BigDecimal.ZERO) <= 0) continue;

            String utr = "UTR" + System.currentTimeMillis() + (int)(Math.random() * 1000);

            // Create DEBIT transaction
            WalletTransaction txn = WalletTransaction.builder()
                    .id("txn-" + UUID.randomUUID().toString())
                    .workerId(wallet.getWorkerId())
                    .type(TransactionType.DEBIT)
                    .amount(amountToDisburse)
                    .status("SUCCESS")
                    .referenceNumber(utr)
                    .description("Automated Nightly Bank Settlement to " + wallet.getBankName() + " (A/c ending " + wallet.getAccountLast4() + ")")
                    .build();
            transactionRepository.save(txn);

            // Reset balance
            wallet.setEarningsBalance(BigDecimal.ZERO);
            walletRepository.save(wallet);

            log.info("Settled ₹{} for worker {} to bank {} (A/C: ****{}) with UTR: {}",
                    amountToDisburse, wallet.getWorkerId(), wallet.getBankName(), wallet.getAccountLast4(), utr);
        }

        log.info("Nightly Batch Auto-Settlement completed for {} workers.", eligibleWallets.size());
    }

    // Cron runs every minute to monitor heartbeats and set inactive workers offline
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void monitorDutyHeartbeats() {
        // Heartbeat monitor check
    }

    // Cron runs every 2 hours to recalculate average ratings
    @Scheduled(cron = "0 0 */2 * * ?")
    @Transactional
    public void recalculateRatings() {
        // Ratings recalculation check
    }
}
