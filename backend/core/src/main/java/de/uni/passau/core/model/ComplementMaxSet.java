package de.uni.passau.core.model;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author pavel.koupil
 */
public class ComplementMaxSet {

    /** Index of the RHS column. */
	public final int forClass;
    /** Max set elements in ascending order. */
	protected List<ColumnSet> elements;
    protected List<ColumnSet> candidates;
	private boolean finalized;

	public ComplementMaxSet(int forClass) {
		this.forClass = forClass;
		this.elements = new LinkedList<ColumnSet>();
        this.candidates = new LinkedList<ColumnSet>();
		this.finalized = false;
	}

    public ComplementMaxSet(int forClass, List<ColumnSet> elements) {
        this.forClass = forClass;
        this.elements = elements;
		this.finalized = false;
    }

	public ComplementMaxSet(int forClass, List<ColumnSet> elements, List<ColumnSet> candidates) {
		this.forClass = forClass;
		this.elements = elements;
		this.candidates = candidates;
		this.finalized = false;
	}

	public void addCandidate(ColumnSet candidate) {
		if (elements.contains(candidate)) {
			throw new IllegalArgumentException("Candidate already exists in elements: " + candidate);
		}
		if (this.candidates == null) {
			this.candidates = new LinkedList<ColumnSet>();
		}
		this.candidates.add(candidate);
	}

	public void moveToTrueMaxSet(ColumnSet candidate) {
		if (candidates == null || !candidates.contains(candidate)) {
			throw new IllegalArgumentException("Candidate does not exist in candidates: " + candidate);
		}
		this.elements.add(candidate);
		this.candidates.remove(candidate);
	}


	public void addCombination(ColumnSet combination) {
		if (candidates != null && candidates.contains(combination)) {
			throw new IllegalArgumentException("Combination already exists in candidates: " + combination + 
											". \nUse moveToTrueMaxSet() to move it to the true max set.");
		}
		this.elements.add(combination);
	}

	public List<ColumnSet> combinations() {
		return this.elements;
	}

    public Stream<ColumnSet> allColumnSets() {
        return Stream.concat(
            elements.stream(),
            candidates.stream()
        );
    }

	@Override
	public String toString() {

		String s = "cmax(" + this.forClass + ": ";
		for (ColumnSet set : this.elements) {
			s += set.convertToLongList();
		}
		return s + ")";
	}

	public void finalize_RENAME_THIS() {

		this.finalized = true;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + forClass;
		result = prime * result + ((elements == null) ? 0 : elements.hashCode());
		result = prime * result + (finalized ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ComplementMaxSet other = (ComplementMaxSet) obj;
		if (forClass != other.forClass) {
			return false;
		}
		if (elements == null) {
			if (other.elements != null) {
				return false;
			}
		} else if (!elements.equals(other.elements)) {
			return false;
		}
		if (finalized != other.finalized) {
			return false;
		}
		return true;
	}

}
