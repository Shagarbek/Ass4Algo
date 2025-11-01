package org.example.graph;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;

public class GraphLoader {

    public static class Edge {
        public int u;
        public int v;
        public double w;
        public Edge() {}
        public Edge(int u, int v, double w) { this.u = u; this.v = v; this.w = w; }
        @Override public String toString() { return String.format("%d->%d(%.2f)", u, v, w); }
    }

    public static class GraphData {
        public boolean directed;
        public int n;
        public List<Edge> edges;
        public int source = 0;
        public String weight_model = "edge";
    }

    public static GraphData loadGraph(String path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(path), GraphData.class);
    }
}
