package com.creditflow.risk;

import com.creditflow.model.ScoredCustomer;
import java.util.*;

/**
 * DSA: Directed Graph (adjacency list), BFS, DFS (3-color cycle detection)
 *
 * Edge A->B: A is a loan guarantor for B.
 * If B defaults, A is liable -> B's default risk propagates to A.
 *
 * BFS cascade: finds all customers impacted if one customer defaults.
 * 3-color DFS: WHITE=unvisited, GRAY=in stack, BLACK=done.
 *              Back edge to GRAY node = cycle found.
 */
public class BorrowerGraph {

    public enum RelationType { GUARANTOR, JOINT_ACCOUNT, BUSINESS_PARTNER }

    public record Edge(String from, String to, RelationType type) {}

    private final Map<String, List<Edge>> adjList = new HashMap<>();
    private final Set<String>             nodes   = new HashSet<>();

    public void registerNode(String id) { nodes.add(id); }

    public void addEdge(String from, String to, RelationType type) {
        if (from.equals(to)) return;
        nodes.add(from);
        nodes.add(to);
        adjList.computeIfAbsent(from, k -> new ArrayList<>())
               .add(new Edge(from, to, type));
    }

    public void addBidirectionalEdge(String a, String b, RelationType type) {
        addEdge(a, b, type);
        addEdge(b, a, type);
    }

    /** BFS cascade: returns list of customers impacted if startId defaults. */
    public List<String> detectCascade(String startId) {
        List<String>  cascadeOrder = new ArrayList<>();
        Set<String>   visited      = new LinkedHashSet<>();
        Queue<String> queue        = new ArrayDeque<>();

        queue.offer(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            cascadeOrder.add(current);
            for (Edge edge : adjList.getOrDefault(current, Collections.emptyList())) {
                if (edge.type == RelationType.GUARANTOR && !visited.contains(edge.to)) {
                    visited.add(edge.to);
                    queue.offer(edge.to);
                }
            }
        }
        if (!cascadeOrder.isEmpty()) cascadeOrder.remove(0);
        return cascadeOrder;
    }

    /** 3-color DFS cycle detection. Returns all cycles found. */
    public List<List<String>> detectCycles() {
        Map<String, Integer> color  = new HashMap<>();
        Map<String, String>  parent = new HashMap<>();
        List<List<String>>   cycles = new ArrayList<>();

        for (String node : nodes) color.put(node, 0);

        for (String node : nodes) {
            if (color.getOrDefault(node, 0) == 0)
                dfsCycle(node, color, parent, cycles);
        }
        return cycles;
    }

    private void dfsCycle(String u, Map<String, Integer> color,
                          Map<String, String> parent,
                          List<List<String>> cycles) {
        color.put(u, 1);
        for (Edge edge : adjList.getOrDefault(u, Collections.emptyList())) {
            String v = edge.to;
            color.putIfAbsent(v, 0);
            if (color.get(v) == 0) {
                parent.put(v, u);
                dfsCycle(v, color, parent, cycles);
            } else if (color.get(v) == 1) {
                List<String> cycle = new ArrayList<>();
                cycle.add(v);
                String cur = u;
                while (cur != null && !cur.equals(v)) {
                    cycle.add(cur);
                    cur = parent.get(cur);
                }
                cycle.add(v);
                Collections.reverse(cycle);
                cycles.add(cycle);
            }
        }
        color.put(u, 2);
    }

    public long cascadeExposure(List<String> cascadeIds,
                                Map<String, ScoredCustomer> scoredMap) {
        return cascadeIds.stream()
            .filter(scoredMap::containsKey)
            .mapToLong(id -> scoredMap.get(id).assignedLimit)
            .sum();
    }

    public UnionFind buildUnionFind() {
        List<String> allNodes = new ArrayList<>(nodes);
        UnionFind uf = new UnionFind(allNodes);
        for (String from : adjList.keySet())
            for (Edge edge : adjList.get(from))
                uf.union(from, edge.to);
        return uf;
    }

    public Set<String> getNodes()         { return Collections.unmodifiableSet(nodes); }
    public int         nodeCount()        { return nodes.size(); }
    public boolean     hasNode(String id) { return nodes.contains(id); }
}
