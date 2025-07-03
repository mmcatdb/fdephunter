package de.uni.passau.algorithms.fd;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import de.uni.passau.core.model.FunctionalDependency;

/**
 *
 * @author pavel.koupil
 */
public class FunctionalDependencyGroup {

	private int attribute = Integer.MIN_VALUE;
	private IntSet values = new IntArraySet();

	public FunctionalDependencyGroup(int attributeID, IntList values) {

		this.attribute = attributeID;
		this.values.addAll(values);
	}

	public int getAttributeID() {

		return this.attribute;
	}

	public IntList getValues() {

		IntList returnValue = new IntArrayList();
		returnValue.addAll(this.values);
		return returnValue;

	}

	@Override
	public String toString() {

		return this.values + " --> " + this.attribute;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + attribute;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		FunctionalDependencyGroup other = (FunctionalDependencyGroup) obj;
		if (attribute != other.attribute) {
			return false;
		}
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}

	public FunctionalDependency buildDependency(String tableIdentifier, List<String> columnNames) {

		FunctionalDependency._ColumnIdentifier[] combination = new FunctionalDependency._ColumnIdentifier[this.values.size()];
		int j = 0;
		for (int i : this.values) {
			combination[j] = new FunctionalDependency._ColumnIdentifier(tableIdentifier, columnNames.get(i));
			j++;
		}
		FunctionalDependency._ColumnCombination cc = new FunctionalDependency._ColumnCombination(combination);
		FunctionalDependency._ColumnIdentifier ci = new FunctionalDependency._ColumnIdentifier(tableIdentifier, columnNames.get(this.attribute));
		FunctionalDependency fd = new FunctionalDependency(cc, ci);
		return fd;
	}

}
