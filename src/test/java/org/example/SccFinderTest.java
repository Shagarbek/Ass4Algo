package org.example;

import org.example.graph.GraphLoader;
import org.example.graph.scc.SccFinder;
import org.example.metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SccFinderTest {

    @Test
    void tarjanSimpleCycle() {
        GraphLoader.GraphData g = new GraphLoader.GraphData();
        g.directed = true; g.n = 3;
        g.edges = List.of(
                new GraphLoader.Edge(0,1,1),
                new GraphLoader.Edge(1,2,1),
                new GraphLoader.Edge(2,0,1)
        );
        Metrics m = new Metrics();
        SccFinder f = new SccFinder(g, m);
        var sccs = f.findSccs();
        assertEquals(1, sccs.size());
        assertEquals(3, sccs.get(0).size());
    }

    @Test
    void tarjanDAG() {
        GraphLoader.GraphData g = new GraphLoader.GraphData();
        g.directed = true; g.n = 3;
        g.edges = List.of(new GraphLoader.Edge(0,1,1), new GraphLoader.Edge(1,2,1));
        Metrics m = new Metrics();
        SccFinder f = new SccFinder(g, m);
        var sccs = f.findSccs();
        assertEquals(3, sccs.size());
    }
    @Test
    void tarjanMultipleSCCs() {
        GraphLoader.GraphData g = new GraphLoader.GraphData();
        g.directed = true;
        g.n = 6;
        g.edges = List.of(
                new GraphLoader.Edge(0, 1, 1.0),
                new GraphLoader.Edge(1, 2, 1.0),
                new GraphLoader.Edge(2, 0, 1.0), // первая SCC
                new GraphLoader.Edge(3, 4, 1.0),
                new GraphLoader.Edge(4, 3, 1.0)  // вторая SCC
        );

        Metrics m = new Metrics();
        SccFinder finder = new SccFinder(g, m);
        var sccs = finder.findSccs();

        assertEquals(3, sccs.size(), "Expected 3 separate SCCs");
        assertTrue(sccs.stream().anyMatch(s -> s.size() == 3));
        assertTrue(sccs.stream().anyMatch(s -> s.size() == 2));
        assertTrue(sccs.stream().anyMatch(s -> s.size() == 1));
    }

}
