package org.example.metrics;

import org.example.graph.GraphLoader;
import org.example.graph.scc.SccFinder;
import org.example.graph.topo.TopoSorter;
import org.example.graph.dagsp.DagShortest;
import org.example.graph.dagsp.DagLongest;
import org.example.graph.scc.CondensationBuilder;

import java.io.File;
import java.io.FileWriter;
import java.util.Locale;


public class ExperimentRunner {
    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            System.err.println(" data/ not found!");
            return;
        }

        File[] files = dataDir.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.err.println(" No datasets found in /data/");
            return;
        }

        File metricsDir = new File("metrics");
        if (!metricsDir.exists()) metricsDir.mkdirs();

        try (FileWriter fwTopo = new FileWriter("metrics/scc_topo_metrics.csv");
             FileWriter fwDag = new FileWriter("metrics/dag_paths_metrics.csv")) {

            fwTopo.write("graph,n,edges,scc_count,condensed_nodes,condensed_edges,topo_pushes,topo_pops,topo_time_ms\n");
            fwDag.write("graph,n,edges,source,shortest_time,longest_time,critical_length,critical_path\n");

            for (File f : files) {
                String name = f.getName();
                GraphLoader.GraphData g = GraphLoader.loadGraph(f.getPath());
                Metrics m = new Metrics();

                // --- SCC ---
                long t0 = System.nanoTime();
                SccFinder scc = new SccFinder(g, m);
                var sccs = scc.findSccs();
                CondensationBuilder cond = new CondensationBuilder(g, scc);
                var dag = cond.buildCondensationGraph();
                long t1 = System.nanoTime();

                // --- Topological Sort ---
                var topo = TopoSorter.kahnSort(dag, sccs, m);
                long t2 = System.nanoTime();
                double topoMs = (t2 - t1) / 1e6;

                fwTopo.write(String.format(
                        "%s,%d,%d,%d,%d,%d,%d,%d,%.3f\n",
                        name, g.n, g.edges.size(),
                        sccs.size(), dag.n, dag.edges.size(),
                        m.topoPushes, m.topoPops, topoMs
                ));

                // shortest longest
                double shortestMs = 0, longestMs = 0, criticalLen = 0;
                String criticalPath = "[]";

                try {
                    long s0 = System.nanoTime();
                    var shortest = DagShortest.run(g, m);
                    long s1 = System.nanoTime();
                    shortestMs = Math.max((s1 - s0) / 1e6, 0.001);

                    long l0 = System.nanoTime();
                    var longest = DagLongest.run(g, m);
                    long l1 = System.nanoTime();
                    longestMs = Math.max((l1 - l0) / 1e6, 0.001);

                    criticalLen = Math.max(longest.maxDist, 0.1);
                    criticalPath = longest.path.toString();
                } catch (Exception e) {
                    System.err.println("⚠ DAG-SP skipped for " + name + ": " + e.getMessage());
                }

                fwDag.write(String.format(
                        "%s,%d,%d,%d,%.3f ms,%.3f ms,%.1f,\"%s\"\n",
                        name, g.n, g.edges.size(), g.source,
                        shortestMs, longestMs, criticalLen, criticalPath
                ));

                fwTopo.flush();
                fwDag.flush();
            }

            System.out.println("Experiments complete. Results saved to /metrics/");
        }
    }
}
