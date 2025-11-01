package org.example.graph.scc;

import org.example.graph.GraphLoader;
import org.example.metrics.Metrics;
import java.util.*;

public class SccFinder {
    private final GraphLoader.GraphData g;
    private final Metrics metrics;

    private int time = 0;
    private int[] disc;
    private int[] low;
    private boolean[] inStack;
    private Deque<Integer> stack;
    private int[] comp; // component id per vertex
    private int compCount = 0;

    public SccFinder(GraphLoader.GraphData g, Metrics metrics) {
        this.g = g;
        this.metrics = metrics;
    }

    public List<List<Integer>> findSccs() {
        int n = g.n;
        disc = new int[n];
        low = new int[n];
        inStack = new boolean[n];
        stack = new ArrayDeque<>();
        comp = new int[n];
        Arrays.fill(disc, -1);

        long t0 = System.nanoTime();
        List<List<Integer>> res = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (disc[i] == -1) dfs(i, res);
        }
        long t1 = System.nanoTime();
        metrics.sccTimeNs += (t1 - t0);
        metrics.sccComponents = res.size();
        return res;
    }

    private void dfs(int u, List<List<Integer>> res) {
        metrics.sccNodeVisits++;
        disc[u] = low[u] = ++time;
        stack.push(u);
        inStack[u] = true;

        // iterate outgoing edges
        if (g.edges != null) {
            for (GraphLoader.Edge e : g.edges) {
                if (e.u != u) continue;
                metrics.sccEdgeVisits++;
                int v = e.v;
                if (disc[v] == -1) {
                    dfs(v, res);
                    low[u] = Math.min(low[u], low[v]);
                } else if (inStack[v]) {
                    low[u] = Math.min(low[u], disc[v]);
                }
            }
        }

        if (low[u] == disc[u]) {
            List<Integer> compList = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                inStack[w] = false;
                comp[w] = compCount;
                compList.add(w);
            } while (w != u);
            compCount++;
            res.add(compList);
        }
    }

    public int[] getComponents() { return comp; }
    public int getCompCount() { return compCount; }
}
