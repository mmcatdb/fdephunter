package de.uni.passau.core.model;

import it.unimi.dsi.fastutil.longs.LongList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class StrippedPartition {

	private final int attribute;
	private final List<LongList> value = new LinkedList<>();
	private boolean finalized = false;

	public StrippedPartition(int attribute) {

		this.attribute = attribute;
	}

	public void addElement(LongList element) {

		if (finalized) {
			return;
		}
		this.value.add(element);
	}

	public void markFinalized() {

		this.finalized = true;
	}

	public int getAttributeID() {

		return this.attribute;
	}

	public List<LongList> getValues() {

		return this.value;

	}

	@Override
	public String toString() {

		String s = "sp(";
		for (LongList il : this.value) {
			s += il.toString() + "-";
		}
		return s + ")";
	}

	public StrippedPartition copy() {
		System.out.println("COPY IS RUNNING IN STRIPPED PARTITION!");
		StrippedPartition copy = new StrippedPartition(this.attribute);
		for (LongList l : this.value) {
			copy.value.add(l);
		}
		copy.finalized = this.finalized;
		return copy;
	}

}
