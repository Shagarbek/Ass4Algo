package org.example.graph;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GraphGenerator {

    public static class Edge {
        public int u;
        public int v;
        public int w;
        public Edge() {}
        public Edge(int u, int v, int w) { this.u = u; this.v = v; this.w = w; }
    }

    public static class GraphData {
        public boolean directed;
        public int n;
        public List<Edge> edges;
        public Integer source;
        public String weight_model;
        public GraphData() {}
        public GraphData(boolean directed, int n, List<Edge> edges, Integer source, String weight_model) {
            this.directed = directed; this.n = n; this.edges = edges;
            this.source = source; this.weight_model = weight_model;
        }
    }

    private static final Random R = new Random(12345); // фиксированное семя для предсказуемости

    public static void main(String[] args) {
        try {
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                boolean ok = dataDir.mkdirs();
                System.out.println("Создана папка data/: " + ok);
            }

            // Жёстко задаём 9 конфигураций (small, medium, large)
            List<Config> configs = List.of(
                    new Config("small_01_dag.json", 6, 0.25, false),
                    new Config("small_02_cycle.json", 6, 0.6, true),
                    new Config("small_03_mixed.json", 8, 0.4, true),

                    new Config("medium_01_mixed.json", 12, 0.35, true),
                    new Config("medium_02_dense.json", 14, 0.7, true),
                    new Config("medium_03_sparse.json", 11, 0.2, false),

                    new Config("large_01_perf_sparse.json", 25, 0.04, false),
                    new Config("large_02_perf_dense.json", 20, 0.25, true),
                    new Config("large_03_random.json", 30, 0.08, true)
            );

            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

            for (Config c : configs) {
                try {
                    GraphData g = generate(c.n, c.density, c.allowCycles);
                    File out = new File(dataDir, sanitize(c.filename));
                    mapper.writeValue(out, g);
                    System.out.printf("Wrote %s (n=%d, edges=%d, %s)%n",
                            out.getPath(), g.n, g.edges.size(), c.allowCycles ? "cyclic/mixed" : "dag");
                } catch (IOException ioe) {
                    System.err.println("Ошибка записи файла для " + c.filename);
                    ioe.printStackTrace();
                }
            }

            System.out.println("Генерация завершена.");
        } catch (Exception e) {
            System.err.println("Фатальная ошибка генератора:");
            e.printStackTrace();
        }
    }

    private static GraphData generate(int n, double density, boolean allowCycles) {
        int maxEdges = n * (n - 1); // directed, no self-loops
        int targetEdges = Math.max(1, (int)(maxEdges * density));
        List<Edge> edges = new ArrayList<>();
        Set<Long> used = new HashSet<>();
        while (edges.size() < targetEdges) {
            int u = R.nextInt(n);
            int v = R.nextInt(n);
            if (u == v) continue;
            if (!allowCycles && u > v) continue; // make DAG by enforcing order
            long key = (((long)u) << 32) | (v & 0xffffffffL);
            if (used.contains(key)) continue;
            used.add(key);
            int w = R.nextInt(9) + 1;
            edges.add(new Edge(u, v, w));
        }
        return new GraphData(true, n, edges, 0, "edge");
    }

    private static String sanitize(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static class Config {
        String filename; int n; double density; boolean allowCycles;
        Config(String filename, int n, double density, boolean allowCycles) {
            this.filename = filename; this.n = n; this.density = density; this.allowCycles = allowCycles;
        }
    }
}
