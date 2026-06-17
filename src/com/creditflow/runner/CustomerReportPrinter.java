package com.creditflow.runner;

import com.creditflow.features.CustomerIndex;
import com.creditflow.features.FeatureExtractor;
import com.creditflow.model.Customer;
import com.creditflow.model.CustomerFeature;
import com.creditflow.model.ScoredCustomer;
import com.creditflow.scoring.ApprovalEngine;
import com.creditflow.scoring.CreditScorer;

/**
 * Prints a full individual credit analysis report for one customer.
 *
 * Pipeline:
 *   Customer -> FeatureExtractor (Deque sliding window)
 *            -> CreditScorer    (weighted formula)
 *            -> ApprovalEngine  (decision tree)
 *            -> CustomerIndex   (TreeMap percentile rank)
 *            -> Print report
 */
public class CustomerReportPrinter {

    private static final String LINE  = "=".repeat(60);
    private static final String DASH  = "-".repeat(60);
    private static final String SDASH = "-".repeat(46);

    public static void print(Customer c, CustomerIndex index) {

        // Step 1: Extract features using Deque sliding window
        CustomerFeature f = FeatureExtractor.extract(c);

        // Step 2: Compute credit score
        int score = CreditScorer.score(f);

        // Step 3: Component breakdown
        double[] bd = CreditScorer.breakdown(f);

        // Step 4: Approval decision
        ScoredCustomer sc = ApprovalEngine.decide(
            new ApprovalEngine.Customer_stub(c.customerId), f, score, c.name);

        // Step 5: Percentile rank from TreeMap index
        double percentile = index.percentileRank(score);

        // ── Print ──────────────────────────────────────────────────────────
        System.out.println();
        System.out.println(LINE);
        System.out.println("  CREDIT ANALYSIS REPORT");
        System.out.printf ("  Customer : %-22s ID: %s%n", c.name, c.customerId);
        System.out.println(LINE);

        // Customer profile
        System.out.println("  CUSTOMER PROFILE");
        System.out.println("  " + SDASH);
        System.out.printf("  Monthly Income          : Rs.%,10.0f%n", c.monthlyIncome);
        System.out.printf("  Existing EMI            : Rs.%,10.0f%n", c.existingEmi);
        System.out.printf("  Missed Payments         : %10d   (last 12 months)%n", c.missedPayments);
        System.out.printf("  Current Credit Balance  : Rs.%,10.0f%n", c.currentBalance);
        System.out.printf("  Current Credit Limit    : Rs.%,10.0f%n", c.currentLimit);
        System.out.printf("  Transactions on file    : %10d%n", c.transactions.size());

        // Behavioral features
        System.out.println();
        System.out.println("  BEHAVIORAL FEATURES  (Deque sliding window - last 90 days)");
        System.out.println("  " + SDASH);
        System.out.printf("  Avg Monthly Spend       : Rs.%,10.0f%n",   f.avgMonthlySpend);
        System.out.printf("  Spend Volatility        : Rs.%,10.0f   (std dev of weekly spend)%n",
                          f.spendVolatility);
        System.out.printf("  Max Single Transaction  : Rs.%,10.0f%n",   f.maxSingleTransaction);
        System.out.printf("  Salary Consistency      : %9.1f%%%n",       f.salaryConsistency * 100);
        System.out.printf("  Utilization Rate        : %9.1f%%%n",       f.utilizationRate * 100);
        System.out.printf("  Debt-to-Income Ratio    : %10.2f%n",        f.debtToIncomeRatio);
        System.out.printf("  Payment History Score   : %10.2f   (1.0 = perfect)%n",
                          f.paymentHistoryScore);

        // Score breakdown
        System.out.println();
        System.out.println("  CREDIT SCORE BREAKDOWN  (CIBIL weighted formula)");
        System.out.println("  " + SDASH);
        System.out.printf("  %-28s (35%%) : %5.0f pts   %s%n",
            "Payment History",     bd[0], missedLabel(c.missedPayments));
        System.out.printf("  %-28s (30%%) : %5.0f pts   %s%n",
            "Credit Utilization",  bd[1], utilizationLabel(f.utilizationRate));
        System.out.printf("  %-28s (20%%) : %5.0f pts   %s%n",
            "Debt-to-Income",      bd[2], dtiLabel(f.debtToIncomeRatio));
        System.out.printf("  %-28s (10%%) : %5.0f pts   %s%n",
            "Spend Stability",     bd[3], volatilityLabel(f.spendVolatility));
        System.out.printf("  %-28s  (5%%) : %5.0f pts   %s%n",
            "Salary Consistency",  bd[4], salaryLabel(f.salaryConsistency));
        System.out.println("  " + SDASH);
        System.out.printf("  %-28s       : %5d / 900%n", "TOTAL CREDIT SCORE", score);
        System.out.printf("  %-28s       : %5.1f%%   (beats this %% of all customers)%n",
            "Percentile Rank", percentile);
        System.out.printf("  %-28s       : %s%n", "Score Band", bandLabel(score));

        // Approval decision
        System.out.println();
        System.out.println("  APPROVAL DECISION");
        System.out.println("  " + SDASH);

        if (sc.approved) {
            System.out.println("  Decision                : ** APPROVED **");
            System.out.printf ("  Credit Tier             : Tier %s%n",     sc.tier);
            System.out.printf ("  Assigned Credit Limit   : Rs.%,d%n",     sc.assignedLimit);
            System.out.printf ("  Interest Rate           : %s%n",          tierRate(sc.tier));
            System.out.printf ("  Required Capital Reserve: Rs.%,d         (Basel III - 8%% of limit)%n",
                               sc.requiredCapital);
            System.out.printf ("  Est. Annual Revenue     : Rs.%,.0f        (bank's interest income)%n",
                               sc.expectedRevenue);
        } else {
            System.out.println("  Decision                : ** REJECTED **");
            System.out.printf ("  Reason                  : %s%n", sc.rejectReason);
            System.out.println();
            System.out.println("  HOW TO IMPROVE YOUR SCORE:");
            printTips(f, score);
        }

        // Risk flags
        System.out.println();
        System.out.println("  RISK FLAGS");
        System.out.println("  " + SDASH);
        printRiskFlags(f, score);

        System.out.println(LINE);
        System.out.println();
    }

    // ── Label helpers ──────────────────────────────────────────────────────

    private static String missedLabel(int m) {
        if (m == 0) return "EXCELLENT - no missed payments";
        if (m == 1) return "GOOD      - 1 missed payment";
        if (m == 2) return "FAIR      - 2 missed payments";
        if (m == 3) return "POOR      - 3 missed payments";
        return "CRITICAL  - " + m + " missed payments";
    }

    private static String utilizationLabel(double u) {
        double p = u * 100;
        if (p < 10)  return String.format("EXCELLENT - %.0f%% used", p);
        if (p < 30)  return String.format("GOOD      - %.0f%% used", p);
        if (p < 50)  return String.format("MODERATE  - %.0f%% used", p);
        if (p < 75)  return String.format("HIGH      - %.0f%% used", p);
        return              String.format("CRITICAL  - %.0f%% used", p);
    }

    private static String dtiLabel(double dti) {
        if (dti < 0.20) return String.format("EXCELLENT - DTI %.2f", dti);
        if (dti < 0.35) return String.format("GOOD      - DTI %.2f", dti);
        if (dti < 0.45) return String.format("MODERATE  - DTI %.2f", dti);
        if (dti < 0.55) return String.format("HIGH      - DTI %.2f (near reject limit)", dti);
        return              String.format("CRITICAL  - DTI %.2f (above reject threshold)", dti);
    }

    private static String volatilityLabel(double v) {
        if (v < 3000)  return "VERY STABLE spending";
        if (v < 8000)  return "STABLE spending";
        if (v < 15000) return "MODERATE volatility";
        if (v < 25000) return "HIGH volatility";
        return              "VERY HIGH volatility";
    }

    private static String salaryLabel(double s) {
        double p = s * 100;
        if (p >= 100) return "PERFECT   - salary every month";
        if (p >= 90)  return String.format("GOOD      - %.0f%% months", p);
        if (p >= 75)  return String.format("MODERATE  - %.0f%% months", p);
        return              String.format("IRREGULAR - only %.0f%% months", p);
    }

    private static String bandLabel(int score) {
        if (score >= 750) return score + "  ->  TIER A  (Excellent)";
        if (score >= 600) return score + "  ->  TIER B  (Good)";
        if (score >= 500) return score + "  ->  TIER C  (Fair - borderline)";
        return                   score + "  ->  BELOW THRESHOLD  (Rejected)";
    }

    private static String tierRate(ScoredCustomer.Tier tier) {
        return switch (tier) {
            case A -> "18% per annum";
            case B -> "24% per annum";
            case C -> "36% per annum";
            default -> "N/A";
        };
    }

    // ── Risk flags ─────────────────────────────────────────────────────────

    private static void printRiskFlags(CustomerFeature f, int score) {
        boolean any = false;
        if (f.missedPayments > 0) {
            System.out.printf("  [!] %d missed payment(s) detected - raises default probability%n",
                              f.missedPayments);
            any = true;
        }
        if (f.utilizationRate > 0.75) {
            System.out.printf("  [!] Utilization %.0f%% - above 75%% danger threshold%n",
                              f.utilizationRate * 100);
            any = true;
        }
        if (f.debtToIncomeRatio > 0.40) {
            System.out.printf("  [!] DTI %.2f - elevated, caution level is 0.40%n",
                              f.debtToIncomeRatio);
            any = true;
        }
        if (f.spendVolatility > 20000) {
            System.out.printf("  [!] High spend volatility Rs.%.0f - unpredictable pattern%n",
                              f.spendVolatility);
            any = true;
        }
        if (f.salaryConsistency < 0.75) {
            System.out.printf("  [!] Salary consistency %.0f%% - irregular income detected%n",
                              f.salaryConsistency * 100);
            any = true;
        }
        if (score < 550 && score >= 500) {
            System.out.printf("  [!] Score %d - approved but very close to rejection boundary%n",
                              score);
            any = true;
        }
        if (!any) System.out.println("  [OK] No significant risk flags detected.");
    }

    // ── Improvement tips ───────────────────────────────────────────────────

    private static void printTips(CustomerFeature f, int score) {
        if (f.missedPayments > 3)
            System.out.println("  -> Clear all overdue payments. Payment history = 35% of score.");
        if (f.utilizationRate > 0.90)
            System.out.println("  -> Reduce credit card balance below 75% of your limit.");
        if (f.debtToIncomeRatio > 0.55)
            System.out.println("  -> Reduce existing EMIs. DTI above 55% = automatic rejection.");
        if (score < 500)
            System.out.println("  -> Reapply after 6 months of consistent on-time payments.");
    }
}
