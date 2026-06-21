package com.banking.bankingserver.service;

import com.banking.bankingserver.dto.BankAccountDTO;
import com.banking.bankingserver.entity.BankAccount;
import com.banking.bankingserver.entity.Transaction;
import com.banking.bankingserver.event.EventType;
import com.banking.bankingserver.event.NotificationEvent;
import com.banking.bankingserver.kafka.EventProducer;
import com.banking.bankingserver.repository.BankAccountRepository;
import com.banking.bankingserver.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BankAccountService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EventProducer eventProducer;

    @Transactional
    public BankAccountDTO openAccount(Long userId, String accountType) {
        String accountNumber = generateAccountNumber();

        BankAccount account = BankAccount.builder()
                .userId(userId)
                .accountNumber(accountNumber)
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .status("ACTIVE")
                .build();

        BankAccount saved = bankAccountRepository.save(account);
        log.info("Bank account opened: userId={}, accountNumber={}", userId, accountNumber);

        publishAccountOpenedEvent(saved);
        return convertToDTO(saved);
    }

    @Transactional
    public BankAccountDTO deposit(Long accountId, BigDecimal amount, String description) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getStatus().equals("CLOSED")) {
            throw new IllegalArgumentException("Cannot deposit to closed account");
        }

        account.setBalance(account.getBalance().add(amount));
        BankAccount saved = bankAccountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .accountId(accountId)
                .type("DEPOSIT")
                .amount(amount)
                .currency(account.getCurrency())
                .description(description != null ? description : "Deposit")
                .referenceId(UUID.randomUUID().toString())
                .status("COMPLETED")
                .build();

        transactionRepository.save(transaction);
        log.info("Deposit completed: accountId={}, amount={}", accountId, amount);

        publishTransactionEvent(saved, transaction, EventType.DEPOSIT_COMPLETED);
        return convertToDTO(saved);
    }

    @Transactional
    public BankAccountDTO withdraw(Long accountId, BigDecimal amount, String description) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getStatus().equals("CLOSED")) {
            throw new IllegalArgumentException("Cannot withdraw from closed account");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        BankAccount saved = bankAccountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .accountId(accountId)
                .type("WITHDRAWAL")
                .amount(amount)
                .currency(account.getCurrency())
                .description(description != null ? description : "Withdrawal")
                .referenceId(UUID.randomUUID().toString())
                .status("COMPLETED")
                .build();

        transactionRepository.save(transaction);
        log.info("Withdrawal completed: accountId={}, amount={}", accountId, amount);

        publishTransactionEvent(saved, transaction, EventType.WITHDRAWAL_COMPLETED);
        return convertToDTO(saved);
    }

    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        BankAccount fromAccount = bankAccountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));

        BankAccount toAccount = bankAccountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        bankAccountRepository.save(fromAccount);
        bankAccountRepository.save(toAccount);

        String referenceId = UUID.randomUUID().toString();

        Transaction fromTransaction = Transaction.builder()
                .accountId(fromAccountId)
                .type("TRANSFER")
                .amount(amount.negate())
                .currency(fromAccount.getCurrency())
                .description("Transfer to " + toAccount.getAccountNumber())
                .referenceId(referenceId)
                .status("COMPLETED")
                .build();

        Transaction toTransaction = Transaction.builder()
                .accountId(toAccountId)
                .type("TRANSFER")
                .amount(amount)
                .currency(toAccount.getCurrency())
                .description("Transfer from " + fromAccount.getAccountNumber())
                .referenceId(referenceId)
                .status("COMPLETED")
                .build();

        transactionRepository.save(fromTransaction);
        transactionRepository.save(toTransaction);

        log.info("Transfer completed: from={}, to={}, amount={}", fromAccountId, toAccountId, amount);
        publishTransferEvent(fromAccount, toAccount, amount, referenceId);
    }

    public BankAccountDTO getAccountById(Long id) {
        return bankAccountRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public List<BankAccountDTO> getAccountsByUserId(Long userId) {
        return bankAccountRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BankAccountDTO> getAllAccounts() {
        return bankAccountRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void publishAccountOpenedEvent(BankAccount account) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(EventType.ACCOUNT_OPENED.name())
                .userId(account.getUserId().toString())
                .accountId(account.getId().toString())
                .accountNumber(account.getAccountNumber())
                .timestamp(LocalDateTime.now())
                .build();

        eventProducer.sendNotificationEvent(event);
    }

    private void publishTransactionEvent(BankAccount account, Transaction transaction, EventType eventType) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(eventType.name())
                .userId(account.getUserId().toString())
                .accountId(account.getId().toString())
                .transactionId(transaction.getId().toString())
                .transactionType(transaction.getType())
                .amount(transaction.getAmount().doubleValue())
                .currency(transaction.getCurrency())
                .timestamp(LocalDateTime.now())
                .build();

        eventProducer.sendNotificationEvent(event);
    }

    private void publishTransferEvent(BankAccount fromAccount, BankAccount toAccount, BigDecimal amount, String referenceId) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(EventType.TRANSFER_COMPLETED.name())
                .userId(fromAccount.getUserId().toString())
                .accountId(fromAccount.getId().toString())
                .transactionType("TRANSFER")
                .amount(amount.doubleValue())
                .currency(fromAccount.getCurrency())
                .timestamp(LocalDateTime.now())
                .payload("Transferred to account " + toAccount.getAccountNumber())
                .build();

        eventProducer.sendNotificationEvent(event);
    }

    private BankAccountDTO convertToDTO(BankAccount account) {
        return BankAccountDTO.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt().toString())
                .updatedAt(account.getUpdatedAt().toString())
                .build();
    }

    private String generateAccountNumber() {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return "BA" + uid;
    }
}
