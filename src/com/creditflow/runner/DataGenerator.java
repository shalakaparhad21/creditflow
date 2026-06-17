package com.creditflow.runner;

import com.creditflow.model.Customer;
import com.creditflow.model.Transaction;
import com.creditflow.model.Transaction.Type;

import java.time.LocalDate;
import java.util.*;

/**
 * Generates synthetic but realistic Indian banking customer profiles.
 *
 * Profile distribution (mirrors RBI urban credit data):
 *   25% Excellent  (income 80K+, low debt, 0 missed payments)
 *   35% Good       (income 40-80K, moderate debt, 0-1 missed)
 *   25% Fair       (income 25-40K, moderate-high debt, 1-2 missed)
 *   15% Poor       (income 10-25K, high debt, 3+ missed)
 */
public class DataGenerator {

    private final Random rng;

    private static final String[] CATEGORIES = {
        "FOOD", "FUEL", "RENT", "EMI", "SHOPPING",
        "MEDICAL", "UTILITIES", "ENTERTAINMENT"
    };

    public DataGenerator(long seed) {
        this.rng = new Random(seed);
    }

    public List<Customer> generate(int n) {
        List<Customer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
            list.add(generateOne(String.format("C%05d", i + 1)));
        return list;
    }

    private Customer generateOne(String id) {
        double roll = rng.nextDouble();
        Profile p;
        if      (roll < 0.25) p = Profile.EXCELLENT;
        else if (roll < 0.60) p = Profile.GOOD;
        else if (roll < 0.85) p = Profile.FAIR;
        else                  p = Profile.POOR;

        double income         = p.incomeMin + rng.nextDouble() * (p.incomeMax - p.incomeMin);
        double existingEmi    = income * (p.emiRatio + rng.nextDouble() * 0.1);
        double currentLimit   = income * (1.0 + rng.nextDouble() * 2.0);
        double utilFrac       = p.utilizationMin + rng.nextDouble() * (p.utilizationMax - p.utilizationMin);
        double currentBalance = currentLimit * utilFrac;
        int missed            = Math.min((int)(rng.nextDouble() * p.maxMissed + 0.5), 12);

        return new Customer(id, "Customer_" + id, income, existingEmi, missed,
                            currentBalance, currentLimit,
                            generateTransactions(id, income, p));
    }

    private List<Transaction> generateTransactions(String id, double income, Profile p) {
        List<Transaction> txns = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusMonths(12);

        for (LocalDate month = start; month.isBefore(today); month = month.plusMonths(1)) {
            if (rng.nextDouble() < p.salaryReliability) {
                txns.add(new Transaction(id,
                    month.withDayOfMonth(1 + rng.nextInt(3)),
                    income * (0.95 + rng.nextDouble() * 0.10),
                    Type.CREDIT, "SALARY"));
            }

            int txnCount = 15 + rng.nextInt(16);
            double monthlySpend = income * (p.spendRatioMin
                + rng.nextDouble() * (p.spendRatioMax - p.spendRatioMin));

            for (int t = 0; t < txnCount; t++) {
                double amt = Math.max(50, (monthlySpend / txnCount) * (0.3 + rng.nextDouble() * 1.5));
                txns.add(new Transaction(id,
                    month.withDayOfMonth(1 + rng.nextInt(28)),
                    amt, Type.DEBIT,
                    CATEGORIES[rng.nextInt(CATEGORIES.length)]));
            }

            if (rng.nextDouble() < 0.7) {
                txns.add(new Transaction(id,
                    month.withDayOfMonth(1),
                    income * (0.15 + rng.nextDouble() * 0.15),
                    Type.DEBIT, "RENT"));
            }
        }

        txns.sort(Comparator.comparing(t -> t.date));
        return txns;
    }

    public List<String[]> generateRelationships(List<Customer> customers, double density) {
        List<String[]> edges = new ArrayList<>();
        String[] types = {"GUARANTOR", "JOINT_ACCOUNT", "BUSINESS_PARTNER"};
        for (int i = 0; i < customers.size(); i++) {
            if (rng.nextDouble() < density) {
                int connections = 1 + rng.nextInt(3);
                for (int c = 0; c < connections; c++) {
                    int j = rng.nextInt(customers.size());
                    if (j != i)
                        edges.add(new String[]{
                            customers.get(i).customerId,
                            customers.get(j).customerId,
                            types[rng.nextInt(types.length)]
                        });
                }
            }
        }
        return edges;
    }

    private enum Profile {
        EXCELLENT(80_000, 200_000, 0.05, 1, 0.05, 0.30, 0.20, 0.40, 0.99),
        GOOD     (40_000,  80_000, 0.15, 2, 0.20, 0.50, 0.25, 0.55, 0.95),
        FAIR     (25_000,  40_000, 0.25, 3, 0.40, 0.70, 0.35, 0.70, 0.85),
        POOR     (10_000,  25_000, 0.35, 6, 0.65, 0.95, 0.50, 0.90, 0.60);

        final double incomeMin, incomeMax, emiRatio, utilizationMin, utilizationMax;
        final double spendRatioMin, spendRatioMax, salaryReliability;
        final int    maxMissed;

        Profile(double incomeMin, double incomeMax, double emiRatio, int maxMissed,
                double utilizationMin, double utilizationMax,
                double spendRatioMin, double spendRatioMax, double salaryReliability) {
            this.incomeMin = incomeMin; this.incomeMax = incomeMax;
            this.emiRatio  = emiRatio;  this.maxMissed = maxMissed;
            this.utilizationMin = utilizationMin; this.utilizationMax = utilizationMax;
            this.spendRatioMin  = spendRatioMin;  this.spendRatioMax  = spendRatioMax;
            this.salaryReliability = salaryReliability;
        }
    }
}
