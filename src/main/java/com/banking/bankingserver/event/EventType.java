package com.banking.bankingserver.event;

public enum EventType {

    USER_REGISTERED("User registered in the system"),
    USER_UPDATED("User profile updated"),
    ACCOUNT_OPENED("Bank account opened"),
    ACCOUNT_CLOSED("Bank account closed"),
    ACCOUNT_FROZEN("Bank account frozen"),
    DEPOSIT_COMPLETED("Deposit transaction completed"),
    WITHDRAWAL_COMPLETED("Withdrawal transaction completed"),
    TRANSFER_COMPLETED("Transfer transaction completed"),
    TRANSFER_FAILED("Transfer transaction failed"),
    PAYMENT_SCHEDULED("Payment scheduled"),
    PAYMENT_PROCESSED("Payment successfully processed"),
    PAYMENT_FAILED("Payment failed");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
