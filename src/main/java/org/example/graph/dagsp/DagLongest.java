package org.example.graph.dagsp;

import org.example.graph.GraphLoader;
import org.example.graph.topo.TopoSorter;
import org.example.metrics.Metrics;

import java.util.*;

public class DagLongest {

    public static class Result {
        public final double maxDist;
        public final List<Integer> path;
        public final long relaxAttempts;
        public final long relaxSuccess;
        public final long timeNs;
        public Result(double maxDist, List<Integer> path, long relaxAttempts, long relaxSuccess, long timeNs) {
            this.maxDist = maxDist; this.path = path; this.relaxAttempts = relaxAttempts; this.relaxSuccess = relaxSuccess; this.timeNs = timeNs;
        }
    }

    public static Result run(GraphLoader.GraphData g, Metrics m) {
        if (g == null) throw new IllegalArgumentException("graph null");
        Metrics metrics = (m == null) ? new Metrics() : m;

        int n = g.n;
        double[] dist = new double[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Double.NEGATIVE_INFINITY);
        Arrays.fill(parent, -1);

        int src = (g.source >=0 && g.source < n) ? g.source : 0;
        dist[src] = 0.0;

        var topoRes = TopoSorter.kahnSort(g, null, metrics);
        List<Integer> topo = topoRes.topoOrder;

        long attempts = 0, succ = 0;
        long t0 = System.nanoTime();
        for (int u : topo) {
            if (Double.isInfinite(dist[u])) continue;
            if (g.edges != null) {
                for (GraphLoader.Edge e : g.edges) {
                    if (e.u != u) continue;
                    attempts++;
                    double cand = dist[u] + e.w;
                    if (cand > dist[e.v]) {
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

        double maxVal = Double.NEGATIVE_INFINITY;
        int target = -1;
        for (int i = 0; i < n; i++) {
            if (dist[i] > maxVal) { maxVal = dist[i]; target = i; }
        }

        List<Integer> path = new ArrayList<>();
        if (target != -1 && !Double.isInfinite(maxVal)) {
            for (int v = target; v != -1; v = parent[v]) path.add(v);
            Collections.reverse(path);
        }

        return new Result(Double.isInfinite(maxVal) ? 0.0 : maxVal, path, attempts, succ, t1 - t0);
    }
}
