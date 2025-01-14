package de.uni.passau.core.approach;

import de.uni.passau.core.graph.Vertex;
import de.uni.passau.core.graph.WeightedGraph;

import java.util.List;

public class FDGraphBuilder {

    /**
     * Build a graph from a list of functional dependencies.
     * @param fds the list of functional dependencies
     * @return the graph
     * 
     * TODO: list of FDs has no weight. We may compute the weight on some criteria later. Currently, all edges have weight 0.
     */
    public WeightedGraph buildGraph(List<FDInit> fds) {
        WeightedGraph graph = new WeightedGraph();

        for (FDInit fd : fds) {
            List<String> lhs = fd.lhs();
            String rhs = fd.rhs();
            
            Vertex rhsVertex = graph.__getVertex(rhs);
            if (rhsVertex == null)
                rhsVertex = graph.__addVertex(rhs);

            Vertex lhsVertex = graph.__getVertex(lhs);
            if (lhsVertex == null)
                lhsVertex = graph.__addVertex(lhs);

            graph.__addEdge(lhsVertex, rhsVertex, 0.0);
        }

        return graph;
    }

}
