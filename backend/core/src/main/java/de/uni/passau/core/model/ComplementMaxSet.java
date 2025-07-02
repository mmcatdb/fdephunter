package de.uni.passau.core.model;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class ComplementMaxSet {

    /** Index of the RHS column. */
	protected int forClass;
    /** Max set elements in ascending order. */
	protected List<ColumnSet> elements;
	private boolean finalized;

	public ComplementMaxSet(int forClass) {
		this.forClass = forClass;
		this.elements = new LinkedList<ColumnSet>();
		this.finalized = false;
	}


    public ComplementMaxSet(int forClass, List<ColumnSet> elements) {
        this.forClass = forClass;
        this.elements = elements;
		this.finalized = false;
    }


	public void addCombination(ColumnSet combination) {

		this.elements.add(combination);
	}

	public List<ColumnSet> getCombinations() {

		return this.elements;
	}

	public int getForClass() {

		return this.forClass;
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
