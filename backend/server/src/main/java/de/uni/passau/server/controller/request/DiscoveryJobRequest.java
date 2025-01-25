package de.uni.passau.server.controller.request;

import java.util.List;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;

public class DiscoveryJobRequest {

    private String description;
    private ApproachName approach;
    private List<String> datasets;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApproachName getApproach() {
        return approach;
    }

    public void setApproach(ApproachName approach) {
        this.approach = approach;
    }

    public List<String> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<String> datasets) {
        this.datasets = datasets;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InitialDiscoveryJobRequest{");
        sb.append("description=").append(description);
        sb.append(", approach=").append(approach);
        sb.append(", datasets=").append(datasets);
        sb.append('}');
        return sb.toString();
    }

}
