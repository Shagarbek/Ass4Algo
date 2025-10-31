package org.example;

import org.example.graph.GraphLoader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Ass4Algo started");

        try {
            var g = GraphLoader.loadGraph("data/medium_01_mixed.json");
            GraphLoader.printGraph(g);
        } catch (IOException e) {
            System.err.println("Error reading graph: " + e.getMessage());
        }
    }
}
