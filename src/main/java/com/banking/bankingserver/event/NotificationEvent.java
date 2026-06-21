package com.banking.bankingserver.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("payee")
    private String payee;

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("payload")
    private Object payload;
}
