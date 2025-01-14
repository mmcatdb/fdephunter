package de.uni.passau.server.clientdto;

import de.uni.passau.core.dataset.Dataset;

import java.io.Serializable;
import java.util.List;

public record DatasetData(
    String[] header,
    List<String[]> rows
) implements Serializable {

    public static DatasetData fromNodes(Dataset dataset, int limit) {
        return new DatasetData(dataset.getHeader(), dataset.getRows().stream().limit(limit).toList());
    }

}
