package com.creditflow.model;

import java.time.LocalDate;

public class Transaction {
    public enum Type { CREDIT, DEBIT }

    public final String    customerId;
    public final LocalDate date;
    public final double    amount;
    public final Type      type;
    public final String    category;

    public Transaction(String customerId, LocalDate date,
                       double amount, Type type, String category) {
        this.customerId = customerId;
        this.date       = date;
        this.amount     = amount;
        this.type       = type;
        this.category   = category;
    }

    public boolean isDebit()  { return type == Type.DEBIT;  }
    public boolean isCredit() { return type == Type.CREDIT; }

    @Override
    public String toString() {
        return String.format("[%s] %s %.0f (%s)", date, type, amount, category);
    }
}
