package com.example.account.model.command;

import java.math.BigDecimal;
import java.util.UUID;

public class ChangeAccount {

    private UUID requestId;
    private UUID personId;
    private BigDecimal sum;

    public ChangeAccount(UUID requestId, UUID personId, BigDecimal sum) {
        this.requestId = requestId;
        this.personId = personId;
        this.sum = sum;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public BigDecimal getSum() {
        return sum;
    }
}
