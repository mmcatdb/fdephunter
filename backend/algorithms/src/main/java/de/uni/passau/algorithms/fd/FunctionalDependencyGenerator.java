package de.uni.passau.algorithms.fd;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.LinkedList;
import java.util.List;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.ColumnSet;

/**
 *
 * @author pavel.koupil
 */
public class FunctionalDependencyGenerator {

	private Dataset fdrr;
	private String relationName;
	private List<String> columns;
	private Int2ObjectMap<List<ColumnSet>> lhss;

	private Exception exception = null;

	private List<FunctionalDependencyGroup> result;

	public FunctionalDependencyGenerator(Dataset fdrr, String relationName, List<String> columnIdentifer, Int2ObjectMap<List<ColumnSet>> lhss) {
		this.fdrr = fdrr;
		this.relationName = relationName;
		this.columns = columnIdentifer;
		this.lhss = lhss;
	}

	public List<FunctionalDependencyGroup> execute() throws Exception {

		this.result = new LinkedList<>();
		for (int attribute : this.lhss.keySet()) {
			for (ColumnSet lhs : this.lhss.get(attribute)) {
				if (lhs.get(attribute)) {
					continue;
				}
				IntList bits = lhs.convertToIntList();

				FunctionalDependencyGroup fdg = new FunctionalDependencyGroup(attribute, bits);
//				this.fdrr.receiveResult((fdg.buildDependency(this.relationName, this.columns)));
				this.result.add(fdg);
			}
		}

		if (this.exception != null) {
			throw this.exception;
		}

		return this.result;
	}

}
