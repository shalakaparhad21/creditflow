package com.creditflow.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    public final String customerId;
    public final String name;
    public final double monthlyIncome;
    public final double existingEmi;
    public final int    missedPayments;
    public final double currentBalance;
    public final double currentLimit;
    public final List<Transaction> transactions;

    public Customer(String customerId, String name, double monthlyIncome,
                    double existingEmi, int missedPayments,
                    double currentBalance, double currentLimit,
                    List<Transaction> transactions) {
        this.customerId     = customerId;
        this.name           = name;
        this.monthlyIncome  = monthlyIncome;
        this.existingEmi    = existingEmi;
        this.missedPayments = missedPayments;
        this.currentBalance = currentBalance;
        this.currentLimit   = currentLimit;
        this.transactions   = transactions != null ? transactions : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("Customer{id=%s, name=%s, income=%.0f}",
                             customerId, name, monthlyIncome);
    }
}
