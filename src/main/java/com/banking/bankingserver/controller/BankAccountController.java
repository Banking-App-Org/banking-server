package com.banking.bankingserver.controller;

import com.banking.bankingserver.dto.BankAccountDTO;
import com.banking.bankingserver.service.BankAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/accounts")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping
    public ResponseEntity<List<BankAccountDTO>> getAllAccounts() {
        try {
            return ResponseEntity.ok(bankAccountService.getAllAccounts());
        } catch (Exception e) {
            log.error("Get all accounts failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<BankAccountDTO> openAccount(@RequestBody Map<String, String> request) {
        try {
            Long userId = Long.parseLong(request.get("userId"));
            String accountType = request.get("accountType");
            BankAccountDTO account = bankAccountService.openAccount(userId, accountType);
            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (Exception e) {
            log.error("Open account failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountDTO> getAccount(@PathVariable Long id) {
        try {
            BankAccountDTO account = bankAccountService.getAccountById(id);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            log.error("Get account failed", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BankAccountDTO>> getAccountsByUser(@PathVariable Long userId) {
        try {
            List<BankAccountDTO> accounts = bankAccountService.getAccountsByUserId(userId);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            log.error("Get accounts by user failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<BankAccountDTO> deposit(@PathVariable Long accountId, @RequestBody Map<String, String> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount"));
            String description = request.get("description");
            BankAccountDTO account = bankAccountService.deposit(accountId, amount, description);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            log.error("Deposit failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<BankAccountDTO> withdraw(@PathVariable Long accountId, @RequestBody Map<String, String> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount"));
            String description = request.get("description");
            BankAccountDTO account = bankAccountService.withdraw(accountId, amount, description);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            log.error("Withdrawal failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestBody Map<String, String> request) {
        try {
            Long fromAccountId = Long.parseLong(request.get("fromAccountId"));
            Long toAccountId = Long.parseLong(request.get("toAccountId"));
            BigDecimal amount = new BigDecimal(request.get("amount"));
            bankAccountService.transfer(fromAccountId, toAccountId, amount);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Transfer failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
