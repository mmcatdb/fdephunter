/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

/**
 *
 * @author pavel.koupil
 */
@Node("Workflow")
public class WorkflowNode {

    public static enum WorkflowState {
        // TODO,
        INITIAL, /* vytvoreno workflow a ceka se na zadani info o datasetu a approach */
        INITIAL_JOB_WAITING, /* bezi proces, prvni discovery job */
        WORKER_ASSIGNMENT, /* mame vysledek discovery procesu a prirazujeme workery -- je totez jako PENDING_ASSIGNMENT */
        JOB_WAITING, /* bezi proces, dalsi faze discovery */
        FINAL /* mame vysledek */;
    }

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private WorkflowState state;

    @Property
    private Integer iteration;

    public static WorkflowNode createNew() {
        final var workflow = new WorkflowNode();
        workflow.setState(WorkflowState.INITIAL);
        workflow.setIteration(0);

        return workflow;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WorkflowState getState() {
        return state;
    }

    public void setState(WorkflowState state) {
        this.state = state;
    }

    public Integer getIteration() {
        return iteration;
    }

    public void setIteration(Integer iteration) {
        this.iteration = iteration;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WorkflowNode{");
        sb.append("id=").append(id);
        sb.append(", state=").append(state);
        sb.append(", iteration=").append(iteration);
        sb.append('}');
        return sb.toString();
    }

}
