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
public class TransactionDTO {

    private Long id;
    private Long accountId;
    private String type;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String referenceId;
    private String status;
    private String createdAt;
}
