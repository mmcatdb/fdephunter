package de.uni.passau.core.model;

/**
 *
 * @author pavel.koupil
 */
public class AgreeSet {

	protected ColumnSet attributes = new ColumnSet();

	public void add(int attribute) {

		this.attributes.set(attribute);
	}

	public ColumnSet getAttributes() {
		return this.attributes.clone();
	}

	@Override
	public String toString() {

		return "ag(" + this.attributes.toIntList().toString() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
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
		AgreeSet other = (AgreeSet) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		return true;
	}

}
