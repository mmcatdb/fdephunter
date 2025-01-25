package de.uni.passau.server.service;

import de.uni.passau.core.nex.Decision;
import de.uni.passau.server.helper.AssignmentConflict;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

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
