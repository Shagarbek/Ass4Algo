package org.example.dagsp;

import org.example.graph.GraphLoader;
import org.example.graph.topo.TopoSorter;

import java.util.*;

public class DagShortest {

    public static class Result {
        public final double[] dist;
        public final int[] parent;
        public final long timeNs;

        public Result(double[] dist, int[] parent, long timeNs) {
            this.dist = dist;
            this.parent = parent;
            this.timeNs = timeNs;
        }

        public List<Integer> reconstructPath(int target) {
            List<Integer> path = new ArrayList<>();
            for (int v = target; v != -1; v = parent[v]) path.add(v);
            Collections.reverse(path);
            return path;
        }
    }

    public static Result shortestPaths(GraphLoader.GraphData dag, int source) {
        int n = dag.n;
        double[] dist = new double[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        // Build adjacency list
        List<List<GraphLoader.Edge>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (GraphLoader.Edge e : dag.edges) {
            adj.get(e.u).add(e);
        }

        // Get topological order
        var topoRes = TopoSorter.kahnSort(dag, null);
        List<Integer> topo = topoRes.topoOrder;

        long t0 = System.nanoTime();
        for (int u : topo) {
            if (dist[u] != Double.POSITIVE_INFINITY) {
                for (GraphLoader.Edge e : adj.get(u)) {
                    if (dist[e.v] > dist[u] + e.w) {
                        dist[e.v] = dist[u] + e.w;
                        parent[e.v] = u;
                    }
                }
            }
        }
        long t1 = System.nanoTime();

        return new Result(dist, parent, t1 - t0);
    }
}
