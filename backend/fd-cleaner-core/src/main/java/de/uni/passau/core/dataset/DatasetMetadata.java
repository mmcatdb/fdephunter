package de.uni.passau.core.dataset;

public interface DatasetMetadata {

    public String getFilepath();

    public String getFilename();

    public long getSize();

    public boolean hasHeader();

    public int getNumberOfColumns();

    public int getNumberOfRows();
}
