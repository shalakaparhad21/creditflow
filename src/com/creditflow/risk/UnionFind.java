package com.creditflow.risk;

import java.util.*;

/**
 * DSA: Union-Find (Disjoint Set Union)
 * Path compression + union by rank = O(alpha(n)) per operation.
 *
 * Banking use: group all connected borrowers into clusters.
 * Cluster aggregate risk = sum of all member credit limits.
 */
public class UnionFind {

    private final Map<String, Integer> idMap;
    private final String[]             ids;
    private final int[]                parent;
    private final int[]                rank;
    private final int[]                size;
    private int                        components;

    public UnionFind(List<String> customerIds) {
        int n        = customerIds.size();
        idMap        = new HashMap<>(n);
        ids          = new String[n];
        parent       = new int[n];
        rank         = new int[n];
        size         = new int[n];
        components   = n;

        for (int i = 0; i < n; i++) {
            idMap.put(customerIds.get(i), i);
            ids[i]    = customerIds.get(i);
            parent[i] = i;
            rank[i]   = 0;
            size[i]   = 1;
        }
    }

    public int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }

    public int find(String id) {
        Integer idx = idMap.get(id);
        if (idx == null) throw new IllegalArgumentException("Unknown: " + id);
        return find(idx);
    }

    public boolean union(String a, String b) {
        if (!idMap.containsKey(a) || !idMap.containsKey(b)) return false;
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
        parent[rb] = ra;
        size[ra]  += size[rb];
        if (rank[ra] == rank[rb]) rank[ra]++;
        components--;
        return true;
    }

    public boolean connected(String a, String b) {
        if (!idMap.containsKey(a) || !idMap.containsKey(b)) return false;
        return find(a) == find(b);
    }

    public int clusterSize(String id) {
        return size[find(id)];
    }

    public Map<String, List<String>> allClusters() {
        Map<Integer, List<String>> byRoot = new HashMap<>();
        for (int i = 0; i < ids.length; i++)
            byRoot.computeIfAbsent(find(i), k -> new ArrayList<>()).add(ids[i]);
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<String>> e : byRoot.entrySet())
            result.put(ids[e.getKey()], e.getValue());
        return result;
    }

    public Map<String, List<String>> connectedClusters() {
        Map<String, List<String>> all = allClusters();
        all.entrySet().removeIf(e -> e.getValue().size() == 1);
        return all;
    }

    public int componentCount() { return components; }
}
