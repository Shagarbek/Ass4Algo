package org.example.metrics;

public class Metrics {
    // SCC
    public long sccNodeVisits = 0;
    public long sccEdgeVisits = 0;
    public long sccComponents = 0;
    public long sccTimeNs = 0;

    // Topological sort
    public long topoPushes = 0;
    public long topoPops = 0;
    public long topoTimeNs = 0;

    // DAG SP
    public long dagRelaxAttempts = 0; // edges considered
    public long dagRelaxSuccess = 0;  // successful relaxations
    public long dagTimeNs = 0;

    public void reset() {
        sccNodeVisits = sccEdgeVisits = sccComponents = sccTimeNs = 0;
        topoPushes = topoPops = topoTimeNs = 0;
        dagRelaxAttempts = dagRelaxSuccess = dagTimeNs = 0;
    }

    public static double nsToMs(long ns){ return ns / 1_000_000.0; }
}
