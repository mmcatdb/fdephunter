/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.uni.passau.core.model;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class MaxSet extends ComplementMaxSet {

	private boolean finalized;

	public MaxSet(int forClass) {
		super(forClass);
		this.finalized = false;
	}

    public MaxSet(int forClass, List<ColumnSet> elements) {
        super(forClass, elements);
		this.finalized = false;
    }

	@Override
	public String toString() {

		String s = "max(" + this.forClass + ": ";
		for (ColumnSet set : this.elements) {
			s += set.convertToLongList();
		}
		return s + ")";
	}

	@Override
	public void finalize_RENAME_THIS() {

		if (!this.finalized) {
			this.checkContentForOnlySuperSets();
		}
		this.finalized = true;

	}

	private void checkContentForOnlySuperSets() {

		List<ColumnSet> superSets = new LinkedList<ColumnSet>();
		List<ColumnSet> toDelete = new LinkedList<ColumnSet>();
		boolean toAdd = true;

		for (ColumnSet set : this.elements) {
			for (ColumnSet superSet : superSets) {
				if (set.isSuperSetOf(superSet)) {
					toDelete.add(superSet);
				}
				if (toAdd) {
					toAdd = !superSet.isSuperSetOf(set);
				}
			}
			superSets.removeAll(toDelete);
			if (toAdd) {
				superSets.add(set);
			} else {
				toAdd = true;
			}
			toDelete.clear();
		}

		this.elements = superSets;
	}
}
