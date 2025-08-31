package de.uni.passau.core.model;

import java.io.Serializable;
import java.util.List;

// The result of an algorithm has to be an object so that it can be properly serialized to a document.
public record Lattices(
    /** On index i, there is a lattice for the class corresponding to the i-th column. */
    List<Lattice> lattices
) implements Serializable {}
