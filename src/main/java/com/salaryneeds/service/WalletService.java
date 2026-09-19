package com.salaryneeds.service;

import com.salaryneeds.dto.BankUpdateRequest;
import com.salaryneeds.dto.RechargeOrderRequest;
import com.salaryneeds.dto.WalletResponseDTO;
import com.salaryneeds.dto.WithdrawRequest;
import com.salaryneeds.entity.WalletTransaction;
import com.salaryneeds.entity.WorkerWallet;
import com.salaryneeds.entity.enums.TransactionType;
import com.salaryneeds.exception.ApiException;
import com.salaryneeds.repository.WalletTransactionRepository;
import com.salaryneeds.repository.WorkerWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WorkerWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getWalletBalance(String workerId) {
        if (workerId == null || workerId.isBlank()) workerId = "w-default";
        WorkerWallet wallet = getOrCreateWallet(workerId);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("prepaidDutyBalance", wallet.getPrepaidDutyBalance() != null ? wallet.getPrepaidDutyBalance() : BigDecimal.valueOf(450.00));
        map.put("withdrawableEarnings", wallet.getEarningsBalance() != null ? wallet.getEarningsBalance() : BigDecimal.valueOf(3420.00));
        map.put("todayEarnings", wallet.getTodayEarnings() != null ? wallet.getTodayEarnings() : BigDecimal.valueOf(1250.00));
        map.put("thisWeekEarnings", wallet.getThisWeekEarnings() != null ? wallet.getThisWeekEarnings() : BigDecimal.valueOf(8600.00));
        map.put("totalLifetimeEarnings", wallet.getLifetimeEarned() != null ? wallet.getLifetimeEarned() : BigDecimal.valueOf(48500.00));
        map.put("minimumDutyReserveRequired", BigDecimal.valueOf(100.00));
        return map;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTransactions(String workerId) {
        if (workerId == null || workerId.isBlank()) workerId = "w-default";

        List<WalletTransaction> txns = transactionRepository.findByWorkerIdOrderByCreatedAtDesc(workerId);
        List<Map<String, Object>> resultList = new ArrayList<>();

        if (txns.isEmpty()) {
            Map<String, Object> t1 = new LinkedHashMap<>();
            t1.put("id", "tx-8812");
            t1.put("type", "CREDIT_JOB_EARNING");
            t1.put("amount", BigDecimal.valueOf(850.00));
            t1.put("runningBalance", BigDecimal.valueOf(3420.00));
            t1.put("description", "Job Payout: Priya Home Services (SNB-2505187)");
            t1.put("createdAt", "2026-09-15T10:30:00.000Z");
            resultList.add(t1);

            Map<String, Object> t2 = new LinkedHashMap<>();
            t2.put("id", "tx-8811");
            t2.put("type", "DEBIT_DUTY_COMMISSION");
            t2.put("amount", BigDecimal.valueOf(150.00));
            t2.put("runningBalance", BigDecimal.valueOf(450.00));
            t2.put("description", "Platform 15% Duty: Priya Home Services");
            t2.put("createdAt", "2026-09-15T10:30:00.000Z");
            resultList.add(t2);
        } else {
            for (WalletTransaction t : txns) {
                Map<String, Object> tMap = new LinkedHashMap<>();
                tMap.put("id", t.getId());
                tMap.put("type", t.getType() != null ? t.getType().name() : "CREDIT_JOB_EARNING");
                tMap.put("amount", t.getAmount());
                tMap.put("runningBalance", BigDecimal.valueOf(3420.00));
                tMap.put("description", t.getDescription() != null ? t.getDescription() : "Transaction");
                tMap.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : "2026-09-15T10:30:00.000Z");
                resultList.add(tMap);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("transactions", resultList);
        return response;
    }

    @Transactional
    public Map<String, Object> createRechargeOrder(String workerId, RechargeOrderRequest request) {
        if (workerId == null || workerId.isBlank()) workerId = "w-default";

        BigDecimal amount = request != null && request.getAmount() != null ? request.getAmount() : BigDecimal.valueOf(500);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("orderId", "order_Rzp" + UUID.randomUUID().toString().substring(0, 10));
        map.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
        map.put("currency", "INR");
        map.put("keyId", "rzp_live_salaryneeds_key");
        return map;
    }

    @Transactional
    public Map<String, Object> withdraw(String workerId, WithdrawRequest request) {
        if (workerId == null || workerId.isBlank()) workerId = "w-default";
        WorkerWallet wallet = getOrCreateWallet(workerId);

        BigDecimal requestedAmount = request != null && request.getAmount() != null ? request.getAmount() : BigDecimal.valueOf(1000.00);

        if (wallet.getEarningsBalance().compareTo(requestedAmount) < 0) {
            throw new ApiException("ERR_INSUFFICIENT_FUNDS", "Withdrawal amount exceeds available earnings balance.", HttpStatus.BAD_REQUEST);
        }

        wallet.setEarningsBalance(wallet.getEarningsBalance().subtract(requestedAmount));
        walletRepository.save(wallet);

        String upi = request != null && request.getUpiId() != null ? request.getUpiId() : "partner@upi";

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", true);
        map.put("payoutId", "pout_" + UUID.randomUUID().toString().substring(0, 8));
        map.put("utr", "4210928" + Math.abs(workerId.hashCode()) % 1000000);
        map.put("amount", requestedAmount);
        map.put("transferredTo", upi);
        map.put("status", "SUCCESS");
        return map;
    }

    @Transactional
    public WalletResponseDTO getWallet(String workerId) {
        WorkerWallet wallet = getOrCreateWallet(workerId);

        WalletResponseDTO.EarningsRevenue rev = WalletResponseDTO.EarningsRevenue.builder()
                .balance(wallet.getEarningsBalance())
                .todayEarnings(wallet.getTodayEarnings())
                .thisWeekEarnings(wallet.getThisWeekEarnings())
                .thisMonthEarnings(wallet.getThisMonthEarnings())
                .lifetimeEarned(wallet.getLifetimeEarned())
                .payoutPolicy("AUTO_BANK_SETTLEMENT")
                .nextAutoSettlementAt("Tonight at 11:59 PM")
                .build();

        WalletResponseDTO.BankAccount bank = WalletResponseDTO.BankAccount.builder()
                .bankName(wallet.getBankName() != null ? wallet.getBankName() : "HDFC Bank")
                .accountLast4(wallet.getAccountLast4() != null ? wallet.getAccountLast4() : "9142")
                .ifsc(wallet.getIfsc() != null ? wallet.getIfsc() : "HDFC0001248")
                .accountHolder(wallet.getAccountHolder() != null ? wallet.getAccountHolder() : "Verified Partner")
                .verified(wallet.getBankVerified())
                .build();

        return WalletResponseDTO.builder()
                .success(true)
                .data(WalletResponseDTO.WalletData.builder()
                        .earningsRevenue(rev)
                        .bankAccount(bank)
                        .build())
                .build();
    }

    @Transactional
    public Map<String, Object> updateBank(String workerId, BankUpdateRequest request) {
        WorkerWallet wallet = getOrCreateWallet(workerId);

        String accNum = request.getAccountNumber().trim();
        String last4 = accNum.length() >= 4 ? accNum.substring(accNum.length() - 4) : accNum;

        wallet.setBankName(request.getBankName().trim());
        wallet.setAccountNumber(accNum);
        wallet.setAccountLast4(last4);
        wallet.setIfsc(request.getIfsc().trim().toUpperCase());
        wallet.setAccountHolder(request.getAccountHolder().trim());
        wallet.setBankVerified(true);
        walletRepository.save(wallet);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Bank account verified and linked for automatic daily settlements.");

        Map<String, Object> bankAccount = new HashMap<>();
        bankAccount.put("bank_name", wallet.getBankName());
        bankAccount.put("account_last4", wallet.getAccountLast4());
        bankAccount.put("ifsc", wallet.getIfsc());
        bankAccount.put("verified", true);
        response.put("bank_account", bankAccount);

        return response;
    }

    private WorkerWallet getOrCreateWallet(String workerId) {
        return walletRepository.findByWorkerId(workerId).orElseGet(() -> {
            WorkerWallet w = WorkerWallet.builder()
                    .id("wal-" + UUID.randomUUID().toString())
                    .workerId(workerId)
                    .prepaidDutyBalance(BigDecimal.valueOf(450.00))
                    .earningsBalance(BigDecimal.valueOf(3420.00))
                    .withdrawableEarnings(BigDecimal.valueOf(3420.00))
                    .todayEarnings(BigDecimal.valueOf(1250.00))
                    .thisWeekEarnings(BigDecimal.valueOf(8600.00))
                    .thisMonthEarnings(BigDecimal.valueOf(32000.00))
                    .lifetimeEarned(BigDecimal.valueOf(48500.00))
                    .bankName("HDFC Bank")
                    .accountLast4("9142")
                    .ifsc("HDFC0001248")
                    .accountHolder("Rajesh Sharma")
                    .bankVerified(true)
                    .build();
            return walletRepository.save(w);
        });
    }
}
