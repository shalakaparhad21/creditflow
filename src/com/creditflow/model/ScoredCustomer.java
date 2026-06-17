package com.creditflow.model;

public class ScoredCustomer {
    public enum Tier { A, B, C, REJECTED }

    public final String  customerId;
    public final String  name;
    public final int     creditScore;
    public final Tier    tier;
    public final boolean approved;
    public final long    assignedLimit;
    public final long    requiredCapital;
    public final double  expectedRevenue;
    public final String  rejectReason;

    public ScoredCustomer(String customerId, String name, int creditScore,
                          Tier tier, boolean approved, long assignedLimit,
                          long requiredCapital, double expectedRevenue,
                          String rejectReason) {
        this.customerId      = customerId;
        this.name            = name;
        this.creditScore     = creditScore;
        this.tier            = tier;
        this.approved        = approved;
        this.assignedLimit   = assignedLimit;
        this.requiredCapital = requiredCapital;
        this.expectedRevenue = expectedRevenue;
        this.rejectReason    = rejectReason;
    }

    @Override
    public String toString() {
        if (!approved)
            return String.format("ScoredCustomer{id=%s, score=%d, REJECTED: %s}",
                                 customerId, creditScore, rejectReason);
        return String.format(
            "ScoredCustomer{id=%s, score=%d, tier=%s, limit=Rs.%,d}",
            customerId, creditScore, tier, assignedLimit);
    }
}
