package de.uni.passau.core.dataset;

import java.util.List;

public interface Dataset {

    public String[] getHeader();

    public List<String[]> getRows();

    public DatasetMetadata getMetadata();

    public void load();

    public void free();

    public boolean isLoaded();

    public interface DatasetMetadata {

        public String filepath();

        public String filename();

        public long size();

        public boolean hasHeader();

        public int numberOfColumns();

        public int numberOfRows();

    }

}
