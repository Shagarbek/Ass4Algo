package org.example.graph.topo;

import org.example.graph.GraphLoader;
import org.example.metrics.Metrics;

import java.util.*;

public class TopoSorter {

    public static class Result {
        public final List<Integer> topoOrder;
        public final List<Integer> expandedOrder;
        public final long pushes;
        public final long pops;
        public final long timeNs;
        public Result(List<Integer> topoOrder, List<Integer> expandedOrder, long pushes, long pops, long timeNs) {
            this.topoOrder = topoOrder;
            this.expandedOrder = expandedOrder;
            this.pushes = pushes;
            this.pops = pops;
            this.timeNs = timeNs;
        }
    }

    public static Result kahnSort(GraphLoader.GraphData dag, List<List<Integer>> sccs, Metrics metrics) {
        if (dag == null) throw new IllegalArgumentException("dag is null");
        Metrics m = (metrics == null) ? new Metrics() : metrics;

        int n = dag.n;
        List<List<Integer>> adj = new ArrayList<>(Math.max(1, n));
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());

        if (dag.edges != null) {
            for (GraphLoader.Edge e : dag.edges) {
                if (e == null) continue;
                if (e.u < 0 || e.u >= n || e.v < 0 || e.v >= n) continue;
                adj.get(e.u).add(e.v);
            }
        }

        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) for (int v : adj.get(u)) indeg[v]++;

        ArrayDeque<Integer> q = new ArrayDeque<>();
        long pushes = 0, pops = 0;
        for (int i = 0; i < n; i++) if (indeg[i] == 0) { q.addLast(i); pushes++; }

        List<Integer> order = new ArrayList<>(n);
        long t0 = System.nanoTime();
        while (!q.isEmpty()) {
            int u = q.removeFirst();
            pops++;
            order.add(u);
            for (int v : adj.get(u)) {
                indeg[v]--;
                if (indeg[v] == 0) { q.addLast(v); pushes++; }
            }
        }
        long t1 = System.nanoTime();

        m.topoPushes += pushes;
        m.topoPops += pops;
        m.topoTimeNs += (t1 - t0);

        if (order.size() != n) {
            System.err.printf("TopoSorter: warning — topo size (%d) != n (%d). Graph may contain a cycle.%n", order.size(), n);
        }

        List<Integer> expanded = new ArrayList<>();
        if (sccs != null) {
            for (int comp : order) {
                if (comp >= 0 && comp < sccs.size()) expanded.addAll(sccs.get(comp));
            }
        } else expanded.addAll(order);

        return new Result(order, expanded, pushes, pops, t1 - t0);
    }
}
