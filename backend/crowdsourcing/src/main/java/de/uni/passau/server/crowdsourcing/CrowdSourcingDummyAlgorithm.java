package de.uni.passau.server.crowdsourcing;

import de.uni.passau.core.approach.FDInit;
import de.uni.passau.core.graph.Vertex;
import de.uni.passau.core.nex.NegativeExample;
import de.uni.passau.server.crowdsourcing.serverdto.Assignment;
import de.uni.passau.server.crowdsourcing.serverdto.ExpertUser;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrowdSourcingDummyAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdSourcingDummyAlgorithm.class);

    public List<FDInit> getFDClassFromVertex(
            Vertex vertex, List<FDInit> fds) {

        if (vertex.__getLabelList().size() != 1) {
            LOGGER.error("to many labels on rhs of (vertex label)");
            return null;
        }
        List<FDInit> labelFDs = new ArrayList<>();
        String label = vertex.__getLabelList().get(0);

        fds.forEach(fd -> {
            if (fd.rhs().equals(label)) {
                labelFDs.add(fd);
            }
        });

        // print labeled FDs
        /*
        System.out.println("## Build FDClass for: " + label);
        StringBuilder sb = new StringBuilder();
        labelFDs.forEach(fd -> {
            List<String> lhs = fd.getFirst();
            for (int i = 0; i < lhs.size(); i++) {
                sb.append( lhs.get(i) );
                if (i < lhs.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("->").append(fd.getSecond()).append("\n");
        });
        System.out.println(sb);
         */
        return labelFDs;
    }

    // TODO: make all possible assignments and not only one?
    public List<Assignment> makeAssignment(List<ExpertUser> idleExperts, List<NegativeExample> unassignedNegativeExamples) {
        if (idleExperts.isEmpty()) {
            LOGGER.info("No idle expert user availible!");
            return List.of();
        }

        if (unassignedNegativeExamples.isEmpty()) {
            LOGGER.info("No negative example availible for assignment!");
            return List.of();
        }

        final List<Assignment> assignments = new ArrayList<>();    // WARN: Service must be state-less

        // try to assign parent examples
        ExpertUser expertUser = null;
        NegativeExample negativeExample = null;

        LOGGER.info("try to assign negative example to user who has worked on the parent");
        for (NegativeExample nex : unassignedNegativeExamples) {

            // TODO here we need an information about the example's previous negative example. It's available as an edge HAS_PREVIOUS_ITERATION, but we don't have it here for some reason. So we should probably change the API of this method.

            if (nex.previousIterationId != null) {
                // try to find user which has processed this negative example
                for (ExpertUser expu : idleExperts) {
                    // Skip user has no last assignment
                    if (expu.getCurrentAssignment() == null)
                        continue;

                    if (expu.getCurrentAssignment().negativeExample.id.equals(nex.previousIterationId)) {
                        LOGGER.info("user found :)");
                        expertUser = expu;
                        break;
                    }
                }

                if (expertUser != null)
                    break;
            }
        }

        if (expertUser == null) {
            LOGGER.info("make assignmente unaware of parent negative example!");
            expertUser = idleExperts.get(0);
            negativeExample = unassignedNegativeExamples.get(0);
        }

        final Assignment assignment = new Assignment(expertUser, negativeExample);
        expertUser.setCurrentAssignment(assignment);
        assignments.add(assignment);

        return assignments;
    }

//    public List<Assignment> getAssignmentList() {
//        return this.assignments;
//    }
}
