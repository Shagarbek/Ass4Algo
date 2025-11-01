package org.example;

import org.example.graph.GraphLoader;
import org.example.graph.dagsp.DagShortest;
import org.example.graph.dagsp.DagLongest;
import org.example.metrics.Metrics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class DagPathTest {

    @Test
    void testShortestAndLongestPath() {
        GraphLoader.GraphData g = new GraphLoader.GraphData();
        g.directed = true;
        g.n = 5;
        g.source = 0;
        g.edges = List.of(
                new GraphLoader.Edge(0, 1, 2.0),
                new GraphLoader.Edge(1, 2, 3.0),
                new GraphLoader.Edge(0, 3, 1.0),
                new GraphLoader.Edge(3, 4, 4.0),
                new GraphLoader.Edge(2, 4, 2.0)
        );
        g.weight_model = "edge";

        Metrics m = new Metrics();
        var shortest = DagShortest.run(g, m);
        var longest = DagLongest.run(g, m);

        assertTrue(shortest.dist[4] > 0, "Shortest path should have positive distance");
        assertTrue(longest.maxDist > 0, "Longest path should be positive");
        assertNotNull(longest.path, "Critical path must exist");
        assertFalse(longest.path.isEmpty(), "Critical path not empty");

        System.out.println("Shortest to node 4: " + shortest.dist[4]);
        System.out.println("Longest path: " + longest.path + " length=" + longest.maxDist);
    }
}
