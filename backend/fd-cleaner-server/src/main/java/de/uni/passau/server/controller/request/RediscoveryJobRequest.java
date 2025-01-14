/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.controller.request;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;

/**
 *
 * @author pavel.koupil
 */
public class RediscoveryJobRequest {

    private String description;
    private ApproachName approach;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RediscoveryJobRequest{");
        sb.append("description=").append(description);
        sb.append(", approach=").append(approach);
        sb.append('}');
        return sb.toString();
    }

}
