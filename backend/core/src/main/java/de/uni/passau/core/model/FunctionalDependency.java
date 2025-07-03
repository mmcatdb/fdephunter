package de.uni.passau.core.model;

import com.google.common.base.Joiner;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.koupil
 */
public class FunctionalDependency {

	public static class _ColumnIdentifier implements Comparable<_ColumnIdentifier>, Serializable {

		private static final long serialVersionUID = -3199299021265706919L;

		public static final String TABLE_COLUMN_CONCATENATOR = ".";
		public static final String TABLE_COLUMN_CONCATENATOR_ESC = "\\.";

		protected String tableIdentifier;
		protected String columnIdentifier;

		public _ColumnIdentifier() {
			this.tableIdentifier = "";
			this.columnIdentifier = "";
		}

		/**
		 * @param tableIdentifier table's identifier
		 * @param columnIdentifier column's identifier
		 */
		public _ColumnIdentifier(String tableIdentifier, String columnIdentifier) {
			this.tableIdentifier = tableIdentifier;
			this.columnIdentifier = columnIdentifier;
		}

		public String getTableIdentifier() {
			return tableIdentifier;
		}

		public void setTableIdentifier(String tableIdentifier) {
			this.tableIdentifier = tableIdentifier;
		}

		public String getColumnIdentifier() {
			return columnIdentifier;
		}

		public void setColumnIdentifier(String columnIdentifier) {
			this.columnIdentifier = columnIdentifier;
		}

		@Override
		public String toString() {
			if (this.tableIdentifier.isEmpty() && this.columnIdentifier.isEmpty()) {
				return "";
			}
			return tableIdentifier + TABLE_COLUMN_CONCATENATOR + columnIdentifier;
		}

		/**
		 * Returns the encoded string for this column identifier. The encoded string is determined by the given
		 * mappings.
		 *
		 * @param tableMapping the table mapping
		 * @param columnMapping the column mapping
		 * @return the encoded string
		 */
		public String toString(Map<String, String> tableMapping, Map<String, String> columnMapping) {
			String tableValue = tableMapping.get(this.tableIdentifier);
			String columnStr = tableValue + TABLE_COLUMN_CONCATENATOR + this.columnIdentifier;
			return columnMapping.get(columnStr);
		}

		/**
		 * Creates a ColumnIdentifier from the given string using the given mappings.
		 *
		 * @param tableMapping the table mapping
		 * @param columnMapping the column mapping
		 * @param str the string
		 * @return a column identifier
		 */
		public static _ColumnIdentifier fromString(Map<String, String> tableMapping, Map<String, String> columnMapping, String str)
				throws NullPointerException, IndexOutOfBoundsException {
			if (str.isEmpty()) {
				return new _ColumnIdentifier();
			}

			String[] parts = columnMapping.get(str).split(TABLE_COLUMN_CONCATENATOR_ESC, 2);
			String tableKey = parts[0];
			String columnName = parts[1];
			String tableName = tableMapping.get(tableKey);

			return new _ColumnIdentifier(tableName, columnName);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((columnIdentifier == null) ? 0 : columnIdentifier.hashCode());
			result = prime * result
					+ ((tableIdentifier == null) ? 0 : tableIdentifier.hashCode());
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
			_ColumnIdentifier other = (_ColumnIdentifier) obj;
			if (columnIdentifier == null) {
				if (other.columnIdentifier != null) {
					return false;
				}
			} else if (!columnIdentifier.equals(other.columnIdentifier)) {
				return false;
			}
			if (tableIdentifier == null) {
				if (other.tableIdentifier != null) {
					return false;
				}
			} else if (!tableIdentifier.equals(other.tableIdentifier)) {
				return false;
			}
			return true;
		}

		@Override
		public int compareTo(_ColumnIdentifier other) {
			int tableIdentifierComparison;
			if (this.tableIdentifier == null) {
				if (other.tableIdentifier == null) {
					tableIdentifierComparison = 0;
				} else {
					tableIdentifierComparison = 1;
				}
			} else if (other.tableIdentifier == null) {
				tableIdentifierComparison = -1;
			} else {
				tableIdentifierComparison = this.tableIdentifier.compareTo(other.tableIdentifier);
			}

			if (0 != tableIdentifierComparison) {
				return tableIdentifierComparison;
			} else {
				return columnIdentifier.compareTo(other.columnIdentifier);
			}
		}
	}

	public static class _ColumnCombination implements Serializable, Comparable<Object> {

		public static final String COLUMN_CONNECTOR = ",";

		private static final long serialVersionUID = -1675606730574675390L;

		protected Set<_ColumnIdentifier> columnIdentifiers;

		/**
		 * Creates an empty column combination. Needed for serialization.
		 */
		public _ColumnCombination() {
			columnIdentifiers = new TreeSet<>();
		}

		/**
		 * Store string identifiers for columns to form a column combination.
		 *
		 * @param columnIdentifier the identifier in the ColumnCombination
		 */
		public _ColumnCombination(_ColumnIdentifier... columnIdentifier) {
			columnIdentifiers = new TreeSet<>(Arrays.asList(columnIdentifier));
		}

		/**
		 * Get column identifiers as set.
		 *
		 * @return columnIdentifiers
		 */
		public Set<_ColumnIdentifier> getColumnIdentifiers() {
			return columnIdentifiers;
		}

		public void setColumnIdentifiers(Set<_ColumnIdentifier> identifiers) {
			this.columnIdentifiers = identifiers;
		}

		@Override
		public String toString() {
			return columnIdentifiers.toString();
		}

		/**
		 * Returns a compressed string representing this column combination.
		 *
		 * @param tableMapping the table mapping
		 * @param columnMapping the column mapping
		 * @return the compressed string
		 */
		public String toString(Map<String, String> tableMapping, Map<String, String> columnMapping) throws NullPointerException {
			List<String> cis = new ArrayList<>();
			for (_ColumnIdentifier ci : this.columnIdentifiers) {
				cis.add(ci.toString(tableMapping, columnMapping));
			}
			return Joiner.on(COLUMN_CONNECTOR).join(cis);
		}

		/**
		 * Creates a column combination from the given string using the given mapping.
		 *
		 * @param tableMapping the table mapping
		 * @param columnMapping the column mapping
		 * @param str the string
		 * @return a column combination
		 */
		public static _ColumnCombination fromString(Map<String, String> tableMapping, Map<String, String> columnMapping, String str)
				throws NullPointerException, IndexOutOfBoundsException {
			String[] parts = str.split(COLUMN_CONNECTOR);

			_ColumnIdentifier[] identifiers = new _ColumnIdentifier[parts.length];
			for (int i = 0; i < parts.length; i++) {
				identifiers[i] = _ColumnIdentifier.fromString(tableMapping, columnMapping, parts[i].trim());
			}
			return new _ColumnCombination(identifiers);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((columnIdentifiers == null) ? 0 : columnIdentifiers
									.hashCode());
			return result;
		}

		@Override
		public int compareTo(Object o) {
			if (o != null && o instanceof _ColumnCombination) {
				_ColumnCombination other = (_ColumnCombination) o;

				int lengthComparison = this.columnIdentifiers.size() - other.columnIdentifiers.size();
				if (lengthComparison != 0) {
					return lengthComparison;

				} else {
					Iterator<_ColumnIdentifier> otherIterator = other.columnIdentifiers.iterator();
					int equalCount = 0;
					int negativeCount = 0;
					int positiveCount = 0;

					while (otherIterator.hasNext()) {
						_ColumnIdentifier currentOther = otherIterator.next();
						// because the order of the single column values can differ,
						// you have to compare all permutations
						for (_ColumnIdentifier currentThis : this.columnIdentifiers) {
							int currentComparison = currentThis.compareTo(currentOther);
							if (currentComparison == 0) {
								equalCount++;
							} else if (currentComparison > 0) {
								positiveCount++;
							} else if (currentComparison < 0) {
								negativeCount++;
							}
						}
					}

					if (equalCount == this.columnIdentifiers.size()) {
						return 0;
					} else if (positiveCount > negativeCount) {
						return 1;
					} else {
						return -1;
					}
				}
			} else {
				//and always last
				return 1;
			}
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
			_ColumnCombination other = (_ColumnCombination) obj;
			if (columnIdentifiers == null) {
				if (other.columnIdentifiers != null) {
					return false;
				}
			} else if (!columnIdentifiers.equals(other.columnIdentifiers)) {
				return false;
			}
			return true;
		}
	}

	public static final String FD_SEPARATOR = "->";

	private static final long serialVersionUID = 7625471410289776666L;

	protected _ColumnCombination determinant;
	protected _ColumnIdentifier dependant;

	public FunctionalDependency() {
		this.dependant = new _ColumnIdentifier();
		this.determinant = new _ColumnCombination();
	}

	public FunctionalDependency(_ColumnCombination determinant,
			_ColumnIdentifier dependant) {
		this.determinant = determinant;
		this.dependant = dependant;
	}

	/**
	 * @return determinant
	 */
	public _ColumnCombination getDeterminant() {
		return determinant;
	}

	public void setDependant(_ColumnIdentifier dependant) {
		this.dependant = dependant;
	}

	/**
	 * @return dependant
	 */
	public _ColumnIdentifier getDependant() {
		return dependant;
	}

	public void setDeterminant(_ColumnCombination determinant) {
		this.determinant = determinant;
	}

//	@XmlTransient
//	public void sendResultTo(OmniscientResultReceiver resultReceiver)
//			throws CouldNotReceiveResultException, ColumnNameMismatchException {
//		resultReceiver.receiveResult(this);
//	}

	@Override
	public String toString() {
		return determinant.toString() + FD_SEPARATOR + dependant.toString();
	}

	/**
	 * Encodes the functional dependency as string with the given mappings.
	 *
	 * @param tableMapping the table mapping
	 * @param columnMapping the column mapping
	 * @return the string
	 */
	public String toString(Map<String, String> tableMapping, Map<String, String> columnMapping) {
		return determinant.toString(tableMapping, columnMapping) + FD_SEPARATOR + dependant.toString(tableMapping, columnMapping);
	}

	/**
	 * Creates a functional dependency from the given string using the given mapping.
	 *
	 * @param tableMapping the table mapping
	 * @param columnMapping the column mapping
	 * @param str the string
	 * @return a functional dependency
	 */
	public static FunctionalDependency fromString(Map<String, String> tableMapping, Map<String, String> columnMapping, String str)
			throws NullPointerException, IndexOutOfBoundsException {
		String[] parts = str.split(FD_SEPARATOR);
		_ColumnCombination determinant = _ColumnCombination.fromString(tableMapping, columnMapping, parts[0]);
		_ColumnIdentifier dependant = _ColumnIdentifier.fromString(tableMapping, columnMapping, parts[1]);

		return new FunctionalDependency(determinant, dependant);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dependant == null) ? 0 : dependant.hashCode());
		result = prime * result
				+ ((determinant == null) ? 0 : determinant.hashCode());
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
		FunctionalDependency other = (FunctionalDependency) obj;
		if (dependant == null) {
			if (other.dependant != null) {
				return false;
			}
		} else if (!dependant.equals(other.dependant)) {
			return false;
		}
		if (determinant == null) {
			if (other.determinant != null) {
				return false;
			}
		} else if (!determinant.equals(other.determinant)) {
			return false;
		}
		return true;
	}
}
