package org.example.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GraphLoader {

    public static class Edge {
        public int u;
        public int v;
        public int w;
    }

    public static class GraphData {
        public boolean directed;
        public int n;
        public List<Edge> edges;
        public Integer source;
        public String weight_model;
    }

    public static GraphData loadGraph(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), GraphData.class);
    }

    public static void printGraph(GraphData g) {
        System.out.println("Directed: " + g.directed);
        System.out.println("Vertices: " + g.n);
        System.out.println("Edges:");
        for (Edge e : g.edges) {
            System.out.println("  " + e.u + " -> " + e.v + " (w=" + e.w + ")");
        }
        System.out.println("Source: " + g.source);
        System.out.println("Weight model: " + g.weight_model);
    }
}
