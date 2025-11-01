package org.example.graph.dagsp;

import org.example.graph.GraphLoader;
import org.example.graph.topo.TopoSorter;
import org.example.metrics.Metrics;

import java.util.*;

public class DagShortest {

    public static class Result {
        public final double[] dist;
        public final int[] parent;
        public final long relaxAttempts;
        public final long relaxSuccess;
        public final long timeNs;
        public Result(double[] dist, int[] parent, long relaxAttempts, long relaxSuccess, long timeNs) {
            this.dist = dist; this.parent = parent; this.relaxAttempts = relaxAttempts;
            this.relaxSuccess = relaxSuccess; this.timeNs = timeNs;
        }

        public List<Integer> reconstructPath(int target) {
            List<Integer> path = new ArrayList<>();
            if (target < 0 || target >= dist.length) return path;
            if (Double.isInfinite(dist[target])) return path;
            for (int v = target; v != -1; v = parent[v]) path.add(v);
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(GraphLoader.GraphData g, Metrics m) {
        if (g == null) throw new IllegalArgumentException("graph null");
        Metrics metrics = (m == null) ? new Metrics() : m;

        int n = g.n;
        double[] dist = new double[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(parent, -1);

        int src = (g.source >=0 && g.source < n) ? g.source : 0;
        dist[src] = 0.0;

        // compute topo on original graph to ensure DAG order; if graph has cycles topo may be incomplete
        var topoRes = TopoSorter.kahnSort(g, null, metrics);
        List<Integer> topo = topoRes.topoOrder;

        long attempts = 0, succ = 0;
        long t0 = System.nanoTime();
        for (int u : topo) {
            if (Double.isInfinite(dist[u])) continue;
            // relax outgoing edges of u
            if (g.edges != null) {
                for (GraphLoader.Edge e : g.edges) {
                    if (e.u != u) continue;
                    attempts++;
                    double cand = dist[u] + e.w;
                    if (cand < dist[e.v]) {
                        dist[e.v] = cand;
                        parent[e.v] = u;
                        succ++;
                    }
                }
            }
        }
        long t1 = System.nanoTime();

        metrics.dagRelaxAttempts += attempts;
        metrics.dagRelaxSuccess += succ;
        metrics.dagTimeNs += (t1 - t0);

        return new Result(dist, parent, attempts, succ, t1 - t0);
    }
}
