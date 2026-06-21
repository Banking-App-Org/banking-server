package com.banking.bankingserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    private Long id;
    private Long userId;
    private Long accountId;
    private String payee;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String scheduledDate;
    private String processedAt;
    private String createdAt;
}
