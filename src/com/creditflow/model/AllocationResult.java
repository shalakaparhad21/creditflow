package com.creditflow.model;

import java.util.List;

public class AllocationResult {
    public final List<ScoredCustomer> selected;
    public final List<ScoredCustomer> deferred;
    public final long   totalCapitalUsed;
    public final long   capitalBudget;
    public final double totalExpectedRevenue;
    public final int    totalApproved;

    public AllocationResult(List<ScoredCustomer> selected,
                            List<ScoredCustomer> deferred,
                            long totalCapitalUsed,
                            long capitalBudget,
                            double totalExpectedRevenue) {
        this.selected             = selected;
        this.deferred             = deferred;
        this.totalCapitalUsed     = totalCapitalUsed;
        this.capitalBudget        = capitalBudget;
        this.totalExpectedRevenue = totalExpectedRevenue;
        this.totalApproved        = selected.size();
    }
}
