package com.creditflow.risk;

import com.creditflow.model.AllocationResult;
import com.creditflow.model.ScoredCustomer;
import java.util.*;

/**
 * DSA: 0/1 Knapsack Dynamic Programming
 *
 * Problem: given N approved customers, each requiring requiredCapital[i]
 * and generating expectedRevenue[i], and a total capital budget W,
 * find the subset maximizing revenue without exceeding W.
 *
 * dp[i][w] = max revenue using first i customers with budget w
 * Recurrence:
 *   dp[i][w] = dp[i-1][w]                                  if cap[i] > w
 *   dp[i][w] = max(dp[i-1][w], dp[i-1][w-cap[i]] + rev[i]) otherwise
 *
 * Time: O(N x W)   Space: O(N x W) for backtracking, O(W) for 1D version
 */
public class CapitalAllocator {

    private static final long SCALE = 1_000L;

    public static AllocationResult allocate(List<ScoredCustomer> approved,
                                            long capitalBudget) {
        if (approved.isEmpty())
            return new AllocationResult(Collections.emptyList(), Collections.emptyList(),
                                        0, capitalBudget, 0);

        int n = approved.size();
        int W = (int) Math.min(capitalBudget / SCALE, 100_000);

        int[]    cap = new int[n];
        double[] rev = new double[n];

        for (int i = 0; i < n; i++) {
            cap[i] = (int) Math.max(1, approved.get(i).requiredCapital / SCALE);
            rev[i] = approved.get(i).expectedRevenue;
        }

        boolean use2D = (long) n * W <= 50_000_000L;
        Set<Integer> selectedIndices = new HashSet<>();

        if (use2D) {
            double[][] dp = new double[n + 1][W + 1];
            for (int i = 1; i <= n; i++) {
                for (int w = 0; w <= W; w++) {
                    dp[i][w] = dp[i-1][w];
                    if (cap[i-1] <= w) {
                        double withItem = dp[i-1][w - cap[i-1]] + rev[i-1];
                        if (withItem > dp[i][w]) dp[i][w] = withItem;
                    }
                }
            }
            int w = W;
            for (int i = n; i >= 1; i--) {
                if (dp[i][w] != dp[i-1][w]) {
                    selectedIndices.add(i - 1);
                    w -= cap[i-1];
                }
            }
        } else {
            double[] dp = new double[W + 1];
            for (int i = 0; i < n; i++)
                for (int w = W; w >= cap[i]; w--)
                    dp[w] = Math.max(dp[w], dp[w - cap[i]] + rev[i]);

            Integer[] order = new Integer[n];
            for (int i = 0; i < n; i++) order[i] = i;
            Arrays.sort(order, (a, b) -> Double.compare(rev[b]/cap[b], rev[a]/cap[a]));
            int remaining = W;
            for (int idx : order) {
                if (cap[idx] <= remaining) {
                    selectedIndices.add(idx);
                    remaining -= cap[idx];
                }
            }
        }

        List<ScoredCustomer> selected = new ArrayList<>();
        List<ScoredCustomer> deferred = new ArrayList<>();
        long   totalCapital = 0;
        double totalRevenue = 0;

        for (int i = 0; i < n; i++) {
            if (selectedIndices.contains(i)) {
                selected.add(approved.get(i));
                totalCapital += approved.get(i).requiredCapital;
                totalRevenue += approved.get(i).expectedRevenue;
            } else {
                deferred.add(approved.get(i));
            }
        }

        return new AllocationResult(selected, deferred, totalCapital, capitalBudget, totalRevenue);
    }
}
