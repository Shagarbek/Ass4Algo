package org.example.graph;

import org.example.graph.scc.*;
import org.example.graph.topo.*;
import org.example.metrics.Metrics;

import java.io.File;

public class GraphBatchTester {
    public static void main(String[] args) throws Exception {
        File[] files = new File("data").listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) {
            System.err.println("No datasets found in /data/");
            return;
        }

        for (File f : files) {
            System.out.println("▶ " + f.getName());
            var g = GraphLoader.loadGraph(f.getPath());

            Metrics m = new Metrics();

            //SCC
            long t0 = System.nanoTime();
            SccFinder scc = new SccFinder(g, m);
            var sccs = scc.findSccs();
            CondensationBuilder builder = new CondensationBuilder(g, scc);
            var dag = builder.buildCondensationGraph();
            long t1 = System.nanoTime();

            //топо
            var topo = TopoSorter.kahnSort(dag, sccs, m);
            long t2 = System.nanoTime();

            System.out.printf("SCCs: %d | Condensed nodes=%d edges=%d%n",
                    sccs.size(), dag.n, dag.edges.size());
            System.out.printf("Topo order size=%d | pushes=%d pops=%d | time=%.3f ms%n",
                    topo.topoOrder.size(), m.topoPushes, m.topoPops, (t2 - t1) / 1e6);
            System.out.printf("SCC time=%.3f ms | total time=%.3f ms%n%n",
                    (t1 - t0) / 1e6, (t2 - t0) / 1e6);
        }
    }
}
