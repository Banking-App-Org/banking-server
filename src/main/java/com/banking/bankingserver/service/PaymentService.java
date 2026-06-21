package com.banking.bankingserver.service;

import com.banking.bankingserver.dto.PaymentDTO;
import com.banking.bankingserver.entity.BankAccount;
import com.banking.bankingserver.entity.Payment;
import com.banking.bankingserver.event.EventType;
import com.banking.bankingserver.event.NotificationEvent;
import com.banking.bankingserver.kafka.EventProducer;
import com.banking.bankingserver.repository.BankAccountRepository;
import com.banking.bankingserver.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private EventProducer eventProducer;

    @Transactional
    public PaymentDTO submitPayment(Long userId, Long accountId, String payee, BigDecimal amount) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Account does not belong to user");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        bankAccountRepository.save(account);

        Payment payment = Payment.builder()
                .userId(userId)
                .accountId(accountId)
                .payee(payee)
                .amount(amount)
                .currency(account.getCurrency())
                .status("PROCESSED")
                .processedAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment submitted: userId={}, payee={}, amount={}", userId, payee, amount);

        publishPaymentEvent(saved);
        return convertToDTO(saved);
    }

    @Transactional
    public PaymentDTO schedulePayment(Long userId, Long accountId, String payee, BigDecimal amount, LocalDate scheduledDate) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Account does not belong to user");
        }

        Payment payment = Payment.builder()
                .userId(userId)
                .accountId(accountId)
                .payee(payee)
                .amount(amount)
                .currency(account.getCurrency())
                .status("PENDING")
                .scheduledDate(scheduledDate)
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment scheduled: userId={}, payee={}, amount={}, date={}", userId, payee, amount, scheduledDate);

        publishScheduledPaymentEvent(saved);
        return convertToDTO(saved);
    }

    public List<PaymentDTO> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentDTO> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    private void publishPaymentEvent(Payment payment) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(EventType.PAYMENT_PROCESSED.name())
                .userId(payment.getUserId().toString())
                .accountId(payment.getAccountId().toString())
                .paymentId(payment.getId().toString())
                .payee(payment.getPayee())
                .amount(payment.getAmount().doubleValue())
                .currency(payment.getCurrency())
                .timestamp(LocalDateTime.now())
                .build();

        eventProducer.sendNotificationEvent(event);
    }

    private void publishScheduledPaymentEvent(Payment payment) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(EventType.PAYMENT_SCHEDULED.name())
                .userId(payment.getUserId().toString())
                .accountId(payment.getAccountId().toString())
                .paymentId(payment.getId().toString())
                .payee(payment.getPayee())
                .amount(payment.getAmount().doubleValue())
                .currency(payment.getCurrency())
                .timestamp(LocalDateTime.now())
                .payload("Scheduled for " + payment.getScheduledDate())
                .build();

        eventProducer.sendNotificationEvent(event);
    }

    private PaymentDTO convertToDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .accountId(payment.getAccountId())
                .payee(payment.getPayee())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .scheduledDate(payment.getScheduledDate() != null ? payment.getScheduledDate().toString() : null)
                .processedAt(payment.getProcessedAt() != null ? payment.getProcessedAt().toString() : null)
                .createdAt(payment.getCreatedAt().toString())
                .build();
    }
}
