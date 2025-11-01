package org.example;

import org.example.graph.GraphLoader;
import org.example.graph.topo.TopoSorter;
import org.example.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TopoSorterTest {

    @Test
    void topoSimpleDAG() {
        GraphLoader.GraphData g = new GraphLoader.GraphData();
        g.directed = true; g.n = 4;
        g.edges = List.of(
                new GraphLoader.Edge(0,1,1),
                new GraphLoader.Edge(1,2,1),
                new GraphLoader.Edge(0,3,1)
        );
        Metrics m = new Metrics();
        var res = TopoSorter.kahnSort(g, null, m);
        assertEquals(4, res.topoOrder.size());
        assertTrue(m.topoPushes > 0);
    }

    @Test
    void topoCycle() {
        GraphLoader.GraphData g = new GraphLoader.GraphData();
        g.directed = true; g.n = 3;
        g.edges = List.of(new GraphLoader.Edge(0,1,1), new GraphLoader.Edge(1,2,1), new GraphLoader.Edge(2,0,1));
        Metrics m = new Metrics();
        var res = TopoSorter.kahnSort(g, null, m);
        assertTrue(res.topoOrder.size() < 3);
    }
}
