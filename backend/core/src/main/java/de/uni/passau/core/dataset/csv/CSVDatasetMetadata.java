package de.uni.passau.core.dataset.csv;

import de.uni.passau.core.dataset.DatasetMetadata;

public class CSVDatasetMetadata implements DatasetMetadata {

    private String filepath;
    private String filename;
    private Boolean hasHeader;
    private long size;
    private int numberOfColumns;
    private int numberOfRows;

    public CSVDatasetMetadata(String filepath, String filename, Boolean hasHeader, long size, int numberOfColumns, int numberOfRows) {
        this.filepath = filepath;
        this.filename = filename;
        this.hasHeader = hasHeader;
        this.size = size;
        this.numberOfColumns = numberOfColumns;
        this.numberOfRows = numberOfRows;
    }

    @Override
    public String getFilepath() {
        return this.filepath;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public boolean hasHeader() {
        return this.hasHeader;
    }

    @Override
    public int getNumberOfColumns() {
        return this.numberOfColumns;
    }

    @Override
    public int getNumberOfRows() {
        return this.numberOfRows;
    }

}
