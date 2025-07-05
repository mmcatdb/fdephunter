package de.uni.passau.algorithms.maxset;

import de.uni.passau.core.dataset.Dataset;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrippedPartitionGenerator {

    private static final String NULL_VALUE_PREFIX = "null#";

    public static List<StrippedPartition> run(Dataset input) {
        /** List indexed by column indexes. */
        final var translationMaps = new ArrayList<Map<String, LongList>>();
        for (int i = 0; i < input.getMetadata().numberOfColumns(); i++)
            translationMaps.add(new HashMap<>());

        long lineNumber = 0;

        for (final String[] row : input.getRows()) {
            for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                String content = row[columnIndex];
                if (content == null)
                    content = NULL_VALUE_PREFIX + Math.random();

                final Map<String, LongList> translationMap = translationMaps.get(columnIndex);
                final LongList element = translationMap.computeIfAbsent(content, x -> new LongArrayList());
                element.add(lineNumber);
            }
            lineNumber++;
        }

        // Loading lists and creating separated partitions
        final var output = new ArrayList<StrippedPartition>();
        for (int columnIndex = 0; columnIndex < translationMaps.size(); columnIndex++)
            output.add(generateStrippedPartition(translationMaps.get(columnIndex), columnIndex));

        return output;
    }

    private static StrippedPartition generateStrippedPartition(Map<String, LongList> translationMap, int forClass) {
        final StrippedPartition sp = new StrippedPartition(forClass);

        for (final LongList it : translationMap.values()) {
            if (it.size() > 1)
                sp.addElement(it);
        }

        return sp;
    }
}
