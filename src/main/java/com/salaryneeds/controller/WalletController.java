package com.salaryneeds.controller;

import com.salaryneeds.dto.BankUpdateRequest;
import com.salaryneeds.dto.RechargeOrderRequest;
import com.salaryneeds.dto.WalletResponseDTO;
import com.salaryneeds.dto.WithdrawRequest;
import com.salaryneeds.security.WorkerContext;
import com.salaryneeds.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/worker/wallet", "/v1/worker/wallet", "/worker", "/v1/worker"})
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getWalletBalance(
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> balance = walletService.getWalletBalance(workerId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> txns = walletService.getTransactions(workerId);
        return ResponseEntity.ok(txns);
    }

    @PostMapping("/recharge/create-order")
    public ResponseEntity<Map<String, Object>> createRechargeOrder(
            @RequestBody(required = false) RechargeOrderRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> order = walletService.createRechargeOrder(workerId, request);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(
            @RequestBody(required = false) WithdrawRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> payout = walletService.withdraw(workerId, request);
        return ResponseEntity.ok(payout);
    }

    @GetMapping
    public ResponseEntity<WalletResponseDTO> getWallet(
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        WalletResponseDTO wallet = walletService.getWallet(workerId);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping({"/bank", "/bank/update"})
    public ResponseEntity<Map<String, Object>> updateBank(
            @Valid @RequestBody BankUpdateRequest request,
            @RequestHeader(value = "X-Worker-Id", required = false) String workerIdHeader) {
        String workerId = WorkerContext.getWorkerId() != null ? WorkerContext.getWorkerId() : workerIdHeader;
        Map<String, Object> response = walletService.updateBank(workerId, request);
        return ResponseEntity.ok(response);
    }
}
