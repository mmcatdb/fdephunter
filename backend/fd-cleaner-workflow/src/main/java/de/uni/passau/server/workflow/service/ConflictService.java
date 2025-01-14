/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow.service;

import de.uni.passau.core.nex.Decision;
import de.uni.passau.server.workflow.helper.AssignmentConflict;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

/**
 *
 * @author pavel.koupil
 */
@Service
public class ConflictService {

    public AssignmentConflict estimateReasonConflicts(List<Decision> decisions) {
        // TODO: THIS HAS TO BE IMPLEMENTED
        return new AssignmentConflict();
    }

    public boolean hasColumnConflicts(List<Decision> decisions) {
        if (decisions.size() < 2)
            return false;
            
        Set<String> firstDecisionColumns = decisions.get(0).getRejectedColumns();
        return decisions.stream().anyMatch(decision -> !decision.getRejectedColumns().equals(firstDecisionColumns));
    }

}
