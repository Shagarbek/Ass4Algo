package org.example.metrics;

import org.example.graph.GraphLoader;
import org.example.graph.scc.SccFinder;
import org.example.graph.scc.CondensationBuilder;
import org.example.graph.topo.TopoSorter;
import org.example.graph.dagsp.DagShortest;
import org.example.graph.dagsp.DagLongest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExperimentRunner {

    public static void main(String[] args) throws Exception {
        File dataDir = new File("data");
        File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.err.println("No .json graphs found in /data directory!");
            return;
        }

        new File("metrics").mkdirs();

        // CSV 1 — SCC + Topo metrics
        try (FileWriter fw = new FileWriter("metrics/metrics_scc_topo.csv")) {
            fw.write("graph,n,edges,scc_count,condensed_nodes,condensed_edges,topo_pushes,topo_pops,topo_time_ms\n");

            for (File f : files) {
                var g = GraphLoader.loadGraph(f.getPath());
                long t0 = System.nanoTime();
                SccFinder sccFinder = new SccFinder(g);
                var sccs = sccFinder.findSccs();
                CondensationBuilder builder = new CondensationBuilder(g, sccFinder);
                var dag = builder.buildCondensationGraph();
                var topo = TopoSorter.kahnSort(dag, sccs);
                long t1 = System.nanoTime();

                fw.write(String.format("%s,%d,%d,%d,%d,%d,%d,%d,%.3f\n",
                        f.getName(), g.n, g.edges.size(),
                        sccs.size(), dag.n, dag.edges.size(),
                        topo.pushes, topo.pops, (t1 - t0) / 1e6));
            }
        }

        // CSV 2 — Path metrics
        try (FileWriter fw = new FileWriter("metrics/metrics_paths.csv")) {
            fw.write("graph,n,edges,source,shortest_time_ms,longest_time_ms,critical_length,critical_path\n");

            for (File f : files) {
                var dag = GraphLoader.loadGraph(f.getPath());
                int src = dag.source;

                var sRes = DagShortest.shortestPaths(dag, src);
                var lRes = DagLongest.longestPath(dag, src);

                fw.write(String.format("%s,%d,%d,%d,%.3f,%.3f,%.1f,\"%s\"\n",
                        f.getName(), dag.n, dag.edges.size(), src,
                        sRes.timeNs / 1e6, lRes.timeNs / 1e6,
                        lRes.length, lRes.reconstructPath()));
            }
        }

        System.out.println("✅ Experiments complete. Results saved to /metrics/");
    }
}
