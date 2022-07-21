package com.example.account.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class SumNotExistException extends Exception {

    public SumNotExistException(UUID personId, BigDecimal sum) {
        super("Person " + personId + " doesn't have needed sum " + sum.longValue());
    }
}
