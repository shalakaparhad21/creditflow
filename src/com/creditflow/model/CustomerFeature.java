package com.creditflow.model;

public class CustomerFeature {
    public final String customerId;
    public final int    missedPayments;
    public final double paymentHistoryScore;
    public final double utilizationRate;
    public final double debtToIncomeRatio;
    public final double monthlyIncome;
    public final double avgMonthlySpend;
    public final double spendVolatility;
    public final double maxSingleTransaction;
    public final double salaryConsistency;

    public CustomerFeature(String customerId,
                           int missedPayments,
                           double paymentHistoryScore,
                           double utilizationRate,
                           double debtToIncomeRatio,
                           double monthlyIncome,
                           double avgMonthlySpend,
                           double spendVolatility,
                           double maxSingleTransaction,
                           double salaryConsistency) {
        this.customerId           = customerId;
        this.missedPayments       = missedPayments;
        this.paymentHistoryScore  = paymentHistoryScore;
        this.utilizationRate      = utilizationRate;
        this.debtToIncomeRatio    = debtToIncomeRatio;
        this.monthlyIncome        = monthlyIncome;
        this.avgMonthlySpend      = avgMonthlySpend;
        this.spendVolatility      = spendVolatility;
        this.maxSingleTransaction = maxSingleTransaction;
        this.salaryConsistency    = salaryConsistency;
    }
}
