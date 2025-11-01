package org.example.graph;

import java.io.FileWriter;
import java.util.*;

public class GraphGenerator {

    private static final Random rand = new Random(42);

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        generateDataset("data/small_01_dag.json", 6, 7, true, false);
        generateDataset("data/small_02_cycle.json", 6, 18, true, true);
        generateDataset("data/small_03_mixed.json", 8, 22, true, true);

        generateDataset("data/medium_01_mixed.json", 12, 46, true, true);
        generateDataset("data/medium_02_dense.json", 14, 127, true, true);
        generateDataset("data/medium_03_sparse.json", 11, 22, true, false);

        generateDataset("data/large_01_perf_sparse.json", 25, 24, true, false);
        generateDataset("data/large_02_perf_dense.json", 20, 95, true, true);
        generateDataset("data/large_03_random.json", 30, 69, true, false);
    }

    private static void generateDataset(String path, int n, int edgeCount, boolean directed, boolean allowCycles) throws Exception {
        GraphLoader.GraphData g = new GraphLoader.GraphData();
        g.directed = directed;
        g.n = n;
        g.edges = new ArrayList<>();

        Set<String> used = new HashSet<>();
        Random rand = new Random();

        // базовое подключение для связности
        for (int i = 0; i < n - 1; i++) {
            int u = i;
            int v = i + 1;
            double w = 1 + rand.nextInt(9); // 1..9
            g.edges.add(new GraphLoader.Edge(u, v, w));
            used.add(u + "->" + v);
        }

        // добавляем случайные рёбра
        while (g.edges.size() < edgeCount) {
            int u = rand.nextInt(n);
            int v = rand.nextInt(n);
            if (u == v) continue;
            if (!allowCycles && u > v) continue;

            String key = u + "->" + v;
            if (used.contains(key)) continue;

            double w = 1 + rand.nextInt(9);
            g.edges.add(new GraphLoader.Edge(u, v, w));
            used.add(key);
        }

        g.source = pickValidSource(g, allowCycles);
        g.weight_model = "edge";

        saveGraph(path, g);
        System.out.printf("✅ %s — n=%d, edges=%d, source=%d%n", path, n, g.edges.size(), g.source);
    }


    private static int pickValidSource(GraphLoader.GraphData g, boolean allowCycles) {
        int[] indeg = new int[g.n];
        for (var e : g.edges) indeg[e.v]++;
        List<Integer> zeroIn = new ArrayList<>();
        for (int i = 0; i < g.n; i++)
            if (indeg[i] == 0) zeroIn.add(i);

        if (!zeroIn.isEmpty()) return zeroIn.get(rand.nextInt(zeroIn.size()));
        return rand.nextInt(g.n);
    }

    private static void saveGraph(String path, GraphLoader.GraphData g) throws Exception {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write("{\n");
            fw.write(String.format(Locale.US, "  \"directed\": %s,%n", g.directed));
            fw.write(String.format("  \"n\": %d,%n", g.n));
            fw.write("  \"edges\": [\n");
            for (int i = 0; i < g.edges.size(); i++) {
                var e = g.edges.get(i);
                fw.write(String.format(Locale.US,
                        "    {\"u\": %d, \"v\": %d, \"w\": %.1f}%s%n",
                        e.u, e.v, e.w, i == g.edges.size() - 1 ? "" : ","));
            }
            fw.write("  ],\n");
            fw.write(String.format("  \"source\": %d,%n", g.source));
            fw.write(String.format("  \"weight_model\": \"%s\"%n", g.weight_model));
            fw.write("}\n");
        }
    }
}
