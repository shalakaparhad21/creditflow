package com.creditflow.runner;

import com.creditflow.model.*;
import com.creditflow.risk.*;
import com.creditflow.scoring.ScoringPipeline;

import java.util.*;

/**
 * Entry point.
 *
 * Mode 1: Batch simulation  - runs all 1000 customers, prints aggregate report
 * Mode 2: Single customer   - enter ID, see full individual credit analysis
 *
 * Data is generated and stored ONCE at startup.
 * Both modes share the same DataStore and ScoringPipeline.
 */
public class Main {

    static final int    NUM_CUSTOMERS  = 1000;
    static final long   CAPITAL_BUDGET = 5_000_000L;  // Rs.50 lakh - realistic constraint
    static final double GRAPH_DENSITY  = 0.15;

    public static void main(String[] args) {

        System.out.println();
        System.out.println("======================================================");
        System.out.println("  CreditFlow - Banking DSA Pipeline");
        System.out.println("======================================================");
        System.out.println();
        System.out.printf("  Generating and loading %,d customer profiles...%n", NUM_CUSTOMERS);

        // Generate data
        DataGenerator    gen           = new DataGenerator(42L);
        List<Customer>   customers     = gen.generate(NUM_CUSTOMERS);
        List<String[]>   relationships = gen.generateRelationships(customers, GRAPH_DENSITY);

        // Load into DataStore for O(1) lookup
        DataStore.load(customers);

        // Run full scoring pipeline (builds SegmentTree + CustomerIndex)
        ScoringPipeline      pipeline  = new ScoringPipeline();
        List<ScoredCustomer> scored    = pipeline.run(customers);

        // Build borrower graph
        BorrowerGraph            graph     = new BorrowerGraph();
        Map<String, ScoredCustomer> scoredMap = new HashMap<>();
        for (ScoredCustomer sc : scored)
            scoredMap.put(sc.customerId, sc);
        customers.forEach(c -> graph.registerNode(c.customerId));
        for (String[] rel : relationships) {
            BorrowerGraph.RelationType type = switch (rel[2]) {
                case "JOINT_ACCOUNT"    -> BorrowerGraph.RelationType.JOINT_ACCOUNT;
                case "BUSINESS_PARTNER" -> BorrowerGraph.RelationType.BUSINESS_PARTNER;
                default                 -> BorrowerGraph.RelationType.GUARANTOR;
            };
            graph.addEdge(rel[0], rel[1], type);
        }

        System.out.printf("  Done. %,d customers ready.%n%n", NUM_CUSTOMERS);

        // Menu loop
        Scanner scanner = new Scanner(System.in);
        while (true) {
            printMenu();
            System.out.print("  Choose (1/2/3): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> runBatchMode(pipeline, scored, graph, relationships, scoredMap);
                case "2" -> runSingleMode(scanner, pipeline);
                case "3" -> {
                    System.out.println();
                    System.out.println("  Goodbye.");
                    System.out.println();
                    return;
                }
                default -> System.out.println("  Invalid. Enter 1, 2, or 3.\n");
            }
        }
    }

    // ── Menu ──────────────────────────────────────────────────────────────

    private static void printMenu() {
        System.out.println("======================================================");
        System.out.println("  MAIN MENU");
        System.out.println("------------------------------------------------------");
        System.out.println("  1.  Batch Report         (all 1000 customers)");
        System.out.println("  2.  Single Customer      (enter customer ID)");
        System.out.println("  3.  Exit");
        System.out.println("------------------------------------------------------");
    }

    // ── MODE 1: Batch ─────────────────────────────────────────────────────

    private static void runBatchMode(ScoringPipeline pipeline,
                                     List<ScoredCustomer> scored,
                                     BorrowerGraph graph,
                                     List<String[]> relationships,
                                     Map<String, ScoredCustomer> scoredMap) {
        System.out.println();
        long t0 = System.currentTimeMillis();

        // Scoring summary
        Map<String, Object> s = pipeline.summary();
        System.out.println("-- SCORING SUMMARY -----------------------------------------");
        System.out.printf("  Total processed : %,d%n",    (int)   s.get("totalProcessed"));
        System.out.printf("  Approved        : %,d%n",    (long)  s.get("approved"));
        System.out.printf("  Rejected        : %,d%n",    (long)  s.get("rejected"));
        System.out.printf("  Approval rate   : %.1f%%%n", (double)s.get("approvalRate"));
        System.out.printf("  Avg credit score: %.0f%n",   (double)s.get("avgScore"));
        System.out.printf("  Total exposure  : Rs.%,.0f%n",(double)((long)s.get("totalExposure")));
        System.out.printf("  Est. annual rev : Rs.%,.0f%n",(double)s.get("totalRevenue"));
        System.out.println();

        // Segment tree range queries
        System.out.println("-- SEGMENT TREE RANGE QUERIES (O(log n)) -------------------");
        int[][] bands   = {{750,900},{600,749},{500,599},{300,499}};
        String[] labels = {"Tier A (750-900)","Tier B (600-749)",
                           "Tier C (500-599)","Rejected (<500)"};
        for (int i = 0; i < bands.length; i++) {
            int  count    = pipeline.getIndex().countInBand(bands[i][0], bands[i][1]);
            long exposure = pipeline.getIndex().totalExposureInBand(bands[i][0], bands[i][1]);
            System.out.printf("  %-20s  count=%-5d  exposure=Rs.%,d%n",
                              labels[i], count, exposure);
        }
        System.out.println();

        // Rejection analysis
        System.out.println("-- REJECTION ANALYSIS --------------------------------------");
        long sRej = scored.stream().filter(sc -> !sc.approved && sc.rejectReason != null
                     && sc.rejectReason.contains("Score")).count();
        long mRej = scored.stream().filter(sc -> !sc.approved && sc.rejectReason != null
                     && sc.rejectReason.contains("missed")).count();
        long dRej = scored.stream().filter(sc -> !sc.approved && sc.rejectReason != null
                     && sc.rejectReason.contains("DTI")).count();
        long uRej = scored.stream().filter(sc -> !sc.approved && sc.rejectReason != null
                     && sc.rejectReason.contains("Utilization")).count();
        System.out.printf("  Score below 500        : %d customers%n", sRej);
        System.out.printf("  Missed payments > 3    : %d customers%n", mRej);
        System.out.printf("  DTI above 55%%          : %d customers%n", dRej);
        System.out.printf("  Utilization above 90%% : %d customers%n", uRej);
        System.out.println();

        // Top 5 riskiest
        System.out.println("-- TOP 5 HIGHEST-RISK APPROVED (Min-Heap) ------------------");
        pipeline.topNRiskiest(5).forEach(sc ->
            System.out.printf("  %s  score=%-4d  tier=%s  limit=Rs.%,d%n",
                              sc.customerId, sc.creditScore, sc.tier, sc.assignedLimit));
        System.out.println();

        // Graph + Union-Find
        UnionFind uf = graph.buildUnionFind();
        Map<String, List<String>> clusters = uf.connectedClusters();
        System.out.println("-- RISK NETWORK (Graph + Union-Find) -----------------------");
        System.out.printf("  Total relationships : %,d%n",    relationships.size());
        System.out.printf("  Total clusters (2+) : %,d%n",    clusters.size());
        System.out.printf("  Largest cluster     : %d members%n",
            clusters.values().stream().mapToInt(List::size).max().orElse(0));
        System.out.println();

        // Cycle detection
        List<List<String>> cycles = graph.detectCycles();
        System.out.println("-- CYCLE DETECTION (3-color DFS) ---------------------------");
        System.out.printf("  Circular guarantor dependencies: %d%n", cycles.size());
        System.out.println();

        // Cascade simulation
        List<ScoredCustomer> approvedList = scored.stream().filter(sc -> sc.approved).toList();
        String cascadeStart = approvedList.stream()
            .filter(sc -> graph.hasNode(sc.customerId))
            .max(Comparator.comparingInt(sc ->
                (int) relationships.stream()
                    .filter(r -> r[0].equals(sc.customerId) && r[2].equals("GUARANTOR"))
                    .count()))
            .map(sc -> sc.customerId)
            .orElse(approvedList.isEmpty() ? null : approvedList.get(0).customerId);

        if (cascadeStart != null) {
            List<String> cascade = graph.detectCascade(cascadeStart);
            long cascExp = graph.cascadeExposure(cascade, scoredMap);
            System.out.println("-- CASCADE SIMULATION (BFS) --------------------------------");
            System.out.printf("  Simulating default by : %s%n",    cascadeStart);
            System.out.printf("  Customers at risk     : %d%n",    cascade.size());
            System.out.printf("  Cascade exposure      : Rs.%,d%n", cascExp);
            System.out.println();
        }

        // Capital allocation (Knapsack DP)
        AllocationResult alloc = CapitalAllocator.allocate(approvedList, CAPITAL_BUDGET);
        System.out.println("-- CAPITAL ALLOCATION (0/1 Knapsack DP) --------------------");
        System.out.printf("  Capital budget    : Rs.%,d%n",   alloc.capitalBudget);
        System.out.printf("  Capital deployed  : Rs.%,d%n",   alloc.totalCapitalUsed);
        System.out.printf("  Utilization       : %.1f%%%n",
            (double) alloc.totalCapitalUsed / alloc.capitalBudget * 100);
        System.out.printf("  Customers funded  : %,d%n",     alloc.selected.size());
        System.out.printf("  Customers deferred: %,d%n",     alloc.deferred.size());
        System.out.printf("  Expected revenue  : Rs.%,.0f%n", alloc.totalExpectedRevenue);
        System.out.println();

        System.out.printf("-- PERFORMANCE  Total: %d ms for %,d customers%n",
                          System.currentTimeMillis() - t0, NUM_CUSTOMERS);
        System.out.println();
    }

    // ── MODE 2: Single customer ───────────────────────────────────────────

    private static void runSingleMode(Scanner scanner, ScoringPipeline pipeline) {
        System.out.println();
        System.out.println("  SINGLE CUSTOMER ANALYSIS");
        System.out.println("  ------------------------");
        System.out.printf ("  %,d customers loaded. Sample IDs:%n", DataStore.size());
        System.out.print  ("  ");
        DataStore.sampleIds(10).forEach(id -> System.out.print(id + "  "));
        System.out.println();
        System.out.println("  (You can enter: C00042  or just  42)");
        System.out.println();

        while (true) {
            System.out.print("  Enter Customer ID (or 'back'): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                System.out.println();
                return;
            }

            String id = normalizeId(input);

            if (!DataStore.exists(id)) {
                System.out.printf("  '%s' not found. Valid range: C00001 to C%05d%n",
                                  id, DataStore.size());
                System.out.println("  Try again or type 'back'.\n");
                continue;
            }

            Customer c = DataStore.get(id);
            CustomerReportPrinter.print(c, pipeline.getIndex());

            System.out.print("  Analyse another customer? (yes/no): ");
            String again = scanner.nextLine().trim().toLowerCase();
            if (again.equals("no") || again.equals("n")) {
                System.out.println();
                return;
            }
            System.out.println();
        }
    }

    /**
     * Normalizes user input to "C00042" format.
     * Accepts: "42", "00042", "c42", "C00042" -> "C00042"
     */
    private static String normalizeId(String input) {
        input = input.trim().toUpperCase();
        if (input.startsWith("C")) input = input.substring(1);
        try {
            return String.format("C%05d", Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return input;
        }
    }
}
