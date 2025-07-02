package de.uni.passau.algorithms.maxset;

import java.util.LinkedList;
import java.util.List;

import de.uni.passau.core.model.AgreeSet;
import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.ComplementMaxSet;
import de.uni.passau.core.model.MaxSet;

/**
 *
 * @author pavel.koupil
 */
public class MaxSetGenerator {

	private List<MaxSet> maxSet;
	private List<ComplementMaxSet> cmaxSet;

	private List<AgreeSet> agreeSets;
	private int numberOfAttributes;

	public MaxSetGenerator(List<AgreeSet> agreeSets, int numberOfAttributes) {
		this.agreeSets = agreeSets;
		this.numberOfAttributes = numberOfAttributes;

	}

	public void targetFD(int columnIndex, int... bits) {
		var _maxSet = maxSet.get(columnIndex);
		AgreeSet s = new AgreeSet();
		for (int index = 0; index < bits.length; ++index) {
			s.add(bits[index]);
		}
		_maxSet.addCombination(s.getAttributes());
	}


	public List<MaxSet> generateMaxSet() throws Exception {

		this.maxSet = new LinkedList<>();

		for (int i = 0; i < this.numberOfAttributes; ++i) {
			// System.out.println("A_index: " + i);
			executeMax_Set_Task(i);
		}

		return maxSet;
	}

	private void executeMax_Set_Task(int currentJob) {

		MaxSet result = new MaxSet(currentJob);
		for (AgreeSet a : this.agreeSets) {
			ColumnSet content = a.getAttributes();
			if (content.get(currentJob)) {
				continue;
			}
			result.addCombination(content);
		}
		result.finalize_RENAME_THIS();
		this.maxSet.add(result);
	}

	public List<ComplementMaxSet> generateCMAX_SETs() throws Exception {

		this.cmaxSet = new LinkedList<>();
		for (int i = 0; i < this.numberOfAttributes; ++i) {
			executeCMAX_SET_Task(i);
		}

		return cmaxSet;
	}

	private void executeCMAX_SET_Task(int currentJob) {

		MaxSet maxSet = null;
		for (MaxSet m : this.maxSet) {
			if (m.getForClass() == currentJob) {
				maxSet = m;
				break;
			}
		}

		ComplementMaxSet result = new ComplementMaxSet(currentJob);

		for (ColumnSet il : maxSet.getCombinations()) {
			ColumnSet inverse = new ColumnSet();
			inverse.set(0, this.numberOfAttributes);
			inverse.xor(il);
			result.addCombination(inverse);
		}

		result.finalize_RENAME_THIS();
		this.cmaxSet.add(result);

	}

}
