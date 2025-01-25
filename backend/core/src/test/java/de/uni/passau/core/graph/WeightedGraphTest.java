package de.uni.passau.core.graph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.uni.passau.core.graph.Edge;
import de.uni.passau.core.graph.Vertex;
import de.uni.passau.core.graph.WeightedGraph;

import java.util.List;

public class WeightedGraphTest {
    @Test
    public void testAddVertex() {
        WeightedGraph graph = new WeightedGraph();
        Vertex vertex = new Vertex("A");
        graph.__addVertex(vertex);
        List<Vertex> vertices = graph.__getVertices();
        Assertions.assertEquals(1, vertices.size());
        Assertions.assertEquals(vertex, vertices.get(0));
    }
    
    @Test
    public void testAddEdge() {
//        WeightedGraph graph = new WeightedGraph();
//        Vertex vertex1 = new Vertex("A");
//        Vertex vertex2 = new Vertex("B");
//        graph.__addVertex(vertex1);
//        graph.__addVertex(vertex2);
//        Edge edge = new Edge(vertex1, vertex2, 5.0);
//        graph.__addEdge(vertex1, vertex2, 5.0);
//        List<Edge> edges = graph.__getEdges(vertex1);
//        Assertions.assertEquals(1, edges.size());
//        Assertions.assertEquals(edge, edges.get(0));
    }
    
    @Test
    public void testGetVertices() {
        WeightedGraph graph = new WeightedGraph();
        Vertex vertex1 = new Vertex("A");
        Vertex vertex2 = new Vertex("B");
        graph.__addVertex(vertex1);
        graph.__addVertex(vertex2);
        List<Vertex> vertices = graph.__getVertices();
        Assertions.assertEquals(2, vertices.size());
        Assertions.assertTrue(vertices.contains(vertex1));
        Assertions.assertTrue(vertices.contains(vertex2));
    }
    
    @Test
    public void testGetEdges() {
//        WeightedGraph graph = new WeightedGraph();
//        Vertex vertex1 = new Vertex("A");
//        Vertex vertex2 = new Vertex("B");
//        graph.__addVertex(vertex1);
//        graph.__addVertex(vertex2);
//        Edge edge = new Edge(vertex1, vertex2, 5.0);
//        graph.__addEdge(vertex1, vertex2, 5.0);
//        List<Edge> edges = graph.__getEdges(vertex1);
//        Assertions.assertEquals(1, edges.size());
//        Assertions.assertEquals(edge, edges.get(0));
    }
    
    @Test
    public void testRemoveEdge() {
        WeightedGraph graph = new WeightedGraph();
        Vertex vertex1 = new Vertex("A");
        Vertex vertex2 = new Vertex("B");
        graph.__addVertex(vertex1);
        graph.__addVertex(vertex2);
        graph.__addEdge(vertex1, vertex2, 5.0);
        graph.__addEdge(vertex2, vertex2, 5.0);
        graph.__removeEdge(vertex1, vertex2);
        List<Edge> edges = graph.__getEdges(vertex1);
        Assertions.assertEquals(0, edges.size());
    }

    @Test
    public void testGetVertexRanked() {
//        WeightedGraph graph = new WeightedGraph();
//        Vertex vertex1 = new Vertex("A");
//        Vertex vertex2 = new Vertex("B");
//        Vertex vertex3 = new Vertex("C");
//        graph.__addVertex(vertex1);
//        graph.__addVertex(vertex2);
//        graph.__addVertex(vertex3);
//        graph.__addEdge(vertex1, vertex2, 5.0);
//        graph.__addEdge(vertex1, vertex3, 10.0);
//        graph.__addEdge(vertex2, vertex3, 15.0);
//        Vertex rankedVertex = graph.__getVertex(0);
//        Assertions.assertEquals(vertex3, rankedVertex);
//        rankedVertex = graph.__getVertex(1);
//        Assertions.assertEquals(vertex2, rankedVertex);
//        rankedVertex = graph.__getVertex(2);
//        Assertions.assertEquals(null, rankedVertex);
//
//        
//        Vertex vertex4 = new Vertex("D");
//        Vertex vertex5 = new Vertex("E");
//        Vertex vertex6 = new Vertex("F");
//        graph.__addVertex(vertex4);
//        graph.__addVertex(vertex5);
//        graph.__addEdge(vertex1, vertex4, 1);
//        graph.__addEdge(vertex1, vertex5, 20);
//        graph.__addEdge(vertex1, vertex5, 20);
//        graph.__addEdge(vertex1, vertex6, 1000);
//        rankedVertex = graph.__getVertex(0);
//        Assertions.assertEquals(vertex5, rankedVertex);
//        rankedVertex = graph.__getVertex(1);
//        Assertions.assertEquals(vertex3, rankedVertex);
//        rankedVertex = graph.__getVertex(2);
//        Assertions.assertEquals(vertex6, rankedVertex);
//        rankedVertex = graph.__getVertex(3);
//        Assertions.assertEquals(vertex2, rankedVertex);
//        rankedVertex = graph.__getVertex(4);
//        Assertions.assertEquals(vertex4, rankedVertex);
        }
}
