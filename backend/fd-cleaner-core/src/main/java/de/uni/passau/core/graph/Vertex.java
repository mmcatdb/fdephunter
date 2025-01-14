package de.uni.passau.core.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a vertex in a graph.
 * A vertex is a fundamental unit of a graph, and represents a point or node in the graph. Each vertex has a label,
 * which is a string that uniquely identifies the vertex within the graph. A vertex can have incoming edges, which are
 * edges that connect to the vertex and have the vertex as their destination.
 * This class is mutable, meaning that its state can be changed after it is constructed.
 */
public class Vertex {

    private List<String> label;
    private List<Edge> incomingEdges;
    private Double weight = 0.0;

    public Vertex() {}

    public List<String> getLabel() {
        return label;
    }

    public void setLabel(List<String> label) {
        this.label = label;
    }

    public List<Edge> getIncomingEdges() {
        return incomingEdges;
    }

    public void setIncomingEdges(List<Edge> incomingEdges) {
        this.incomingEdges = incomingEdges;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * Constructs a new vertex with the given label.
     *
     * @param label the label of the vertex
     */
    public Vertex(String label) {
        this.label = new ArrayList<>();
        this.label.add(label);
        incomingEdges = new ArrayList<>();
    }

    public Vertex(List<String> label) {
        // Copy because we are going to modify the list.
        this.label = new ArrayList<>(label);
        // Order labels alphabetically.
        this.label.sort(String::compareToIgnoreCase);
        incomingEdges = new ArrayList<>();
    }

    private static final String IDENTIFIER_DELIMITER = ",";

    public static String labelToIdentifier(List<String> label) {
        return String.join(IDENTIFIER_DELIMITER, label);
    }

    public static List<String> identifierToLabel(String identifier) {
        return List.of(identifier.split(IDENTIFIER_DELIMITER));
    }

    /**
     * Adds the given edge to the list of incoming edges for this vertex.
     *
     * @param edge the incoming edge to add
     */
    protected void __addIncomingEdge(Edge edge) {
        incomingEdges.add(edge);
        weight += edge.__getWeight();
    }

    /**
     * Removes the given edge from the list of incoming edges for this vertex.
     *
     * @param edge the incoming edge to remove
     */
    protected void __removeIncomingEdge(Edge edge) {
        incomingEdges.remove(edge);
        weight -= edge.__getWeight();
    }

    /**
     * Returns a list of the incoming edges for this vertex.
     *
     * @return a list of the incoming edges for this vertex
     */
    public List<Edge> __getIncomingEdges() {
        return incomingEdges;
    }

    /**
     * Returns the label of this vertex.
     *
     * @return the label of this vertex
     */
    public String __getLabel() {
        return labelToIdentifier(label);
    }

    public List<String> __getLabelList() {
        return label;
    }

    /**
     * Returns a string representation of the vertex.
     *
     * @return a string representation of the vertex
     */
    @Override
    public String toString() {
        return String.format("Vertex %s with %d incoming edges", label, incomingEdges.size());
    }

    /**
     * Returns true if this vertex is equal to the given object.
     *
     * @param o the object to compare to this vertex
     * @return true if this vertex is equal to the given object, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vertex vertex = (Vertex) o;
        return Objects.equals(label, vertex.label) && Objects.equals(incomingEdges, vertex.incomingEdges);
    }

    public double __getWeight() {
        return weight;
    }

}
