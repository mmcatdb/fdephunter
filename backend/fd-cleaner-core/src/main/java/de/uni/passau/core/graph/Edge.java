package de.uni.passau.core.graph;

import java.util.Objects;

/**
 * Represents a weighted edge in a graph.
 * An edge connects two vertices (the source and destination vertices) and has a weight. The weight of an edge is a
 * numerical value that represents the cost or distance associated with traversing the edge.
 * This class is immutable, meaning that its state cannot be changed after it is constructed. To change the weight of an
 * edge, a new edge object must be created with the updated weight.
 */
public class Edge {

    private String source;
    private String destination;
    private Double weight = 0.0;

    /**
     * Constructs a new edge with the given source and destination vertices and weight.
     *
     * @param source the source vertex id of the edge
     * @param destination the destination vertex id of the edge
     * @param weight the weight of the edge
     */
    public Edge(String source, String destination, Double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Edge() {}

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * Returns the source vertex id of the edge.
     *
     * @return the source vertex id of the edge
     */
    public String __getSource() {
        return source;
    }

    /**
     * Returns the destination vertex id of the edge.
     *
     * @return the destination vertex id of the edge
     */
    public String __getDestination() {
        return destination;
    }

    /**
     * Returns the weight of the edge.
     *
     * @return the weight of the edge
     */
    public double __getWeight() {
        return weight;
    }

    /**
     * Sets the weight of the edge to the given value.
     *
     * @param weight the new weight of the edge
     */
    public void __setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * Returns a string representation of the edge.
     *
     * @return a string representation of the edge
     */
    @Override
    public String toString() {
        return String.format("(%s -> %s, %.2f)", source, destination, weight);
    }

    /**
     * Returns true if this edge is equal to the given object.
     *
     * @param o the object to compare to this edge
     * @return true if this edge is equal to the given object, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Edge edge = (Edge) o;
        return Double.compare(edge.weight, weight) == 0
                && Objects.equals(source, edge.source)
                && Objects.equals(destination, edge.destination);
    }
}
