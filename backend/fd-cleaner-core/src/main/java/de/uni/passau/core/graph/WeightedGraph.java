package de.uni.passau.core.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a weighted graph.
 * A weighted graph is a graph in which each edge has a weight or cost associated with it. This class represents a
 * weighted graph using an adjacency list data structure.
 * This class is mutable, meaning that its state can be changed after it is constructed.
 */
public class WeightedGraph {

    private Map<String, Vertex> vertices;
    private Map<Vertex, List<Edge>> adjacencyList; // unnecessary?
    private List<Vertex> rankedVertices;
    private List<Edge> edges;

    /**
     * Constructs a new, empty weighted graph.
     */
    public WeightedGraph() {
        // TODO: dependency injection, inversion of control.
        adjacencyList = new HashMap<>();
        rankedVertices = new ArrayList<>();
        vertices = new HashMap<>();
        edges = new ArrayList<>();
    }

    public Map<String, Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(Map<String, Vertex> vertices) {
        this.vertices = vertices;
    }

    public Map<Vertex, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    public void setAdjacencyList(Map<Vertex, List<Edge>> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    public List<Vertex> getRankedVertices() {
        return rankedVertices;
    }

    public void setRankedVertices(List<Vertex> rankedVertices) {
        this.rankedVertices = rankedVertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    /**
     * Adds the given vertex to the graph.
     * If the vertex is already in the graph, this method has no effect.
     *
     * @param vertex the vertex to add to the graph
     */
    public void __addVertex(Vertex vertex) throws IllegalArgumentException {
        String label = vertex.__getLabel();
        if (vertices.containsKey(label)) {
            throw new IllegalArgumentException("Vertex is already in the graph");
        }
        vertices.put(label, vertex);
        adjacencyList.putIfAbsent(vertex, new ArrayList<>());
    }

    public Vertex __addVertex(String label) {
        Vertex vertex = new Vertex(label);
        __addVertex(vertex);
        return vertex;
    }

    public Vertex __addVertex(List<String> label) {
        Vertex vertex = new Vertex(label);
        __addVertex(vertex);
        return vertex;
    }

    /**
     * Adds a weighted edge to the graph between the given source and destination vertices.
     * If either the source or destination vertex is not in the graph, it is added to the graph.
     *
     * @param source the source vertex of the edge
     * @param destination the destination vertex of the edge
     * @param weight the weight of the edge
     */
    public void __addEdge(Vertex source, Vertex destination, Double weight) throws IllegalArgumentException {
        if (!adjacencyList.containsKey(source))
            throw new IllegalArgumentException("Source vertex is not in the graph");

        if (!adjacencyList.containsKey(destination))
            throw new IllegalArgumentException("Destination vertex is not in the graph");

        String sourceLabel = source.__getLabel();
        String destinationLabel = destination.__getLabel();
        Edge edge = new Edge(sourceLabel, destinationLabel, weight);
        destination.__addIncomingEdge(edge);
        adjacencyList.get(source).add(edge);
        edges.add(edge);
        __computeRankedVertices();
    }

    /**
     * Returns a list of the edges that are adjacent to the given vertex.
     * If the vertex is not in the graph, this method returns null.
     *
     * @param vertex the vertex to get the adjacent edges for
     * @return a list of the edges that are adjacent to the given vertex, or null if the vertex is not in the graph
     */
    public List<Edge> __getEdges(Vertex vertex) {
        return adjacencyList.get(vertex);
    }

    /**
     * Returns a list of all the vertices in the graph.
     *
     * @return a list of all the vertices in the graph
     */
    public List<Vertex> __getVertices() {
        return new ArrayList<>(adjacencyList.keySet());
    }

    /**
     * Removes the edge between the given source and destination vertices from the graph.
     * If the edge is not in the graph, this method has no effect.
     *
     * @param source the source vertex of the edge to remove
     * @param destination the destination vertex of the edge to remove
     */
    public void __removeEdge(Vertex source, Vertex destination) {
        List<Edge> edges = adjacencyList.get(source);
        for (Edge edge : edges) {
            if (edge.__getDestination().equals(destination.__getLabel())) {
                edges.remove(edge);
                destination.__removeIncomingEdge(edge);
                __computeRankedVertices();
                return;
            }
        }
    }

    public void __removeVertex(Vertex vertex) {
        String label = vertex.__getLabel();
        for (Edge edge : edges) {
            if (edge.__getSource().equals(label) || edge.__getDestination().equals(label)) {
                edges.remove(edge);
            }
        }
        adjacencyList.remove(vertex);
        vertices.remove(label);
        __computeRankedVertices();
    }

    public Vertex __getVertex(String label) {
        return vertices.containsKey(label) ? vertices.get(label) : null;
    }

    public Vertex __getVertex(List<String> label) {
        return __getVertex(Vertex.labelToIdentifier(label));
    }

    /**
     * Returns the vertex with the given ranking. Only vertices with incoming edges are considered.
     * If the ranking is negative, this method throws an IllegalArgumentException.
     * If the ranking is greater than or equal to the number of vertices with incoming edges, this method returns null.
     * 
     * @param ranking the ranking of the vertex to get
     * @return the vertex with the given ranking, or null if the ranking is greater than or equal to the number of vertices with incoming edges
     */
    public Vertex __getVertex(int ranking) throws IllegalArgumentException {
        if (ranking < 0)
            throw new IllegalArgumentException("Ranking must be non-negative");

        return ranking >= rankedVertices.size() ? null : rankedVertices.get(ranking);
    }

    public List<Vertex> __getRankedVertices() {
        return rankedVertices;
    }

    /**
     * Ranks the vertices in the graph based on their indegree and weight.
     * TODO: Currently we completely recompute the ranking every time this method is called. This is inefficient.
     */
    private void __computeRankedVertices() {
        rankedVertices.clear();
        // add all vertices with incoming edges
        for (Vertex vertex : vertices.values()) {
            if (!vertex.__getIncomingEdges().isEmpty()) {
                rankedVertices.add(vertex);
            }
        }

        rankedVertices.sort((v1, v2) -> {
            final int indegreeCompare = Integer.compare(v2.__getIncomingEdges().size(), v1.__getIncomingEdges().size());
            return indegreeCompare != 0 ? indegreeCompare : Double.compare(v2.__getWeight(), v1.__getWeight());
        });
    }

    @Override
    public String toString() {
        return String.join("\n", edges.stream().map(Edge::toString).toArray(String[]::new)) + "\n";
    }

}
