package com.creditflow.runner;

import com.creditflow.model.Customer;
import java.util.*;

/**
 * DSA: HashMap<String, Customer>
 *
 * Holds all generated customers in memory for O(1) lookup by ID.
 * Loaded once at startup. Used by Mode 2 (single customer analysis).
 *
 * In a real bank this would be a database query.
 * Here it is an in-memory HashMap acting as our "database".
 */
public class DataStore {

    private static final Map<String, Customer> store = new LinkedHashMap<>();
    private static List<Customer> allCustomers        = new ArrayList<>();

    public static void load(List<Customer> customers) {
        store.clear();
        allCustomers = new ArrayList<>(customers);
        for (Customer c : customers)
            store.put(c.customerId.toUpperCase(), c);
    }

    /** O(1) lookup by customer ID. Returns null if not found. */
    public static Customer get(String customerId) {
        return store.get(customerId.trim().toUpperCase());
    }

    public static List<Customer> getAll() {
        return Collections.unmodifiableList(allCustomers);
    }

    public static boolean exists(String customerId) {
        return store.containsKey(customerId.trim().toUpperCase());
    }

    public static int size() { return store.size(); }

    /** Returns sample IDs for the user to try — first 5 and last 5. */
    public static List<String> sampleIds(int n) {
        List<String> keys   = new ArrayList<>(store.keySet());
        List<String> sample = new ArrayList<>();
        for (int i = 0; i < Math.min(n / 2, keys.size()); i++)
            sample.add(keys.get(i));
        for (int i = Math.max(0, keys.size() - n / 2); i < keys.size(); i++)
            sample.add(keys.get(i));
        return sample;
    }
}
