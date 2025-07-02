/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.uni.passau.algorithms.maxset;

import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.model.StrippedPartition;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pavel.koupil
 */
public class StrippedPartitionGenerator {

	public static String nullValue = "null#" + Math.random();

	private List<StrippedPartition> returnValue;

	private Int2ObjectMap<Map<String, LongList>> translationMaps = new Int2ObjectOpenHashMap<>();

	public StrippedPartitionGenerator() {
	}

	public List<StrippedPartition> execute(Dataset input) throws Exception {

		int lineNumber = 0;
		List<String[]> rows = input.getRows();

		for (String[] row : rows) {
			for (int column = 0; column < row.length; ++column) {
				String content = row[column];
				if (null == content) {
					content = StrippedPartitionGenerator.nullValue;
				}

				Map<String, LongList> translationMap;
				if ((translationMap = this.translationMaps.get(column)) == null) {
					translationMap = new HashMap<>();
					this.translationMaps.put(column, translationMap);
				}
				LongList element;
				if ((element = translationMap.get(content)) == null) {
					element = new LongArrayList();
					translationMap.put(content, element);
				}
				element.add(lineNumber);
			}
			lineNumber++;
		}

		// Loading lists and creating separated partitions
		this.returnValue = new LinkedList<>();
		for (int i : this.translationMaps.keySet()) {
			executeStrippedPartitionGenerationTask(i);
		}

		// cleanup
		this.translationMaps.clear();

		return this.returnValue;

	}

	private void executeStrippedPartitionGenerationTask(int i) {

		StrippedPartition sp = new StrippedPartition(i);
		this.returnValue.add(sp);

		Map<String, LongList> toItterate = this.translationMaps.get(i);

		for (LongList it : toItterate.values()) {

			if (it.size() > 1) {
				sp.addElement(it);
			}

		}

		// cleanup after work
		this.translationMaps.get(i).clear();
	}

}
