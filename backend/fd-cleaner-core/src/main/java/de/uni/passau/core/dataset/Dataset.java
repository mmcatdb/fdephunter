package de.uni.passau.core.dataset;

public interface Dataset extends DatasetData {

    public void load();

    public void free();

    public DatasetMetadata getMetadata();

    public boolean isLoaded();

}
