package org.example.graph.scc;

import org.example.graph.GraphLoader;

import java.util.*;

public class CondensationBuilder {
    private final GraphLoader.GraphData g;
    private final SccFinder finder;

    public CondensationBuilder(GraphLoader.GraphData g, SccFinder finder) {
        this.g = g;
        this.finder = finder;
    }

    public GraphLoader.GraphData buildCondensationGraph() {
        int[] comp = finder.getComponents();
        int compCount = finder.getCompCount();

        Set<String> seen = new HashSet<>();
        List<GraphLoader.Edge> edges = new ArrayList<>();

        if (g.edges != null) {
            for (GraphLoader.Edge e : g.edges) {
                int cu = comp[e.u];
                int cv = comp[e.v];
                if (cu != cv) {
                    String key = cu + "->" + cv;
                    if (seen.add(key)) {
                        edges.add(new GraphLoader.Edge(cu, cv, e.w));
                    }
                }
            }
        }

        GraphLoader.GraphData dag = new GraphLoader.GraphData();
        dag.directed = true;
        dag.n = compCount;
        dag.edges = edges;
        dag.source = 0;
        dag.weight_model = g.weight_model;
        return dag;
    }
}
