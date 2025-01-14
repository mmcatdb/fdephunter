package de.uni.passau.server.workflow;

import de.uni.passau.core.approach.FDGraphBuilder;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.core.dataset.csv.CSVDataset;
import de.uni.passau.core.graph.Vertex;
import de.uni.passau.core.graph.WeightedGraph;
import de.uni.passau.core.nex.Decision;
import de.uni.passau.core.nex.Decision.ColumnReason;
import de.uni.passau.core.nex.Decision.PredefinedReason;
import de.uni.passau.core.nex.NegativeExample;
import de.uni.passau.core.nex.NegativeExampleBuilder;
import de.uni.passau.server.approach.service.HyFDService;
import de.uni.passau.server.approach.service.OurApproachService;
import de.uni.passau.server.crowdsourcing.serverdto.Assignment;
import de.uni.passau.server.crowdsourcing.serverdto.ExpertUser;
import de.uni.passau.server.crowdsourcing.service.CrowdSourcingDummyService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CrowdSourcingWorkflow {

    // CSV-Dataset
    private static final String datasetPath = HyFDService.class.getClassLoader().getResource("WDC_satellites.csv").getPath();
    private static final Dataset dataset = new CSVDataset(
        datasetPath,
        false
    );

    // List of IDLE expert user
    private static List<ExpertUser> expertUserList = new ArrayList<>();
    // List of unassigned negative examples
    // Tuple(Vertex-Label, NegativeExample)
    private static List<NegativeExample> negativeExamplesList = new ArrayList<>();

    // FD-Discovering Service
    private static OurApproachService fdDiscovering = new OurApproachService();
    // Neg Builder
    private static NegativeExampleBuilder negativeExampleBuilder;
    // Crownsourcing Service
    private static CrowdSourcingDummyService crowdSourcingDummyService = new CrowdSourcingDummyService();

    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdSourcingWorkflow.class);

    public static void main(String... args) {

        // Add some dummy user
        expertUserList.add(new ExpertUser("0"));
        expertUserList.add(new ExpertUser("1"));
        expertUserList.add(new ExpertUser("2"));
        expertUserList.add(new ExpertUser("3"));
        expertUserList.add(new ExpertUser("4"));

        // Discover the FDs from given dataset
        dataset.load();
        var discoveredFDs = fdDiscovering.execute(dataset.getHeader(), dataset.getRows());

        // Build the weighted graph out of the discovered FDs
        WeightedGraph weightedGraph = new FDGraphBuilder().buildGraph(discoveredFDs);

        // Get ranked vertex list
        List<Vertex> orderedVertexList = weightedGraph.__getRankedVertices();

        System.out.println("\n\n## Ordered Vertex List:");
        for (Vertex v : orderedVertexList) {
            System.out.println(v.toString());
        }

        // Initialize the nagative example builder
        negativeExampleBuilder = new NegativeExampleBuilder(dataset);

        // Building initial list of negative examples
        for (final Vertex vertex : orderedVertexList) {
            //System.out.println("# NextVertex: " + vertex.toString());
            // get list containing only FDs with "label" on rhs
            var fdClass = crowdSourcingDummyService.getFDClassFromVertex(vertex, discoveredFDs);

            negativeExamplesList.add(negativeExampleBuilder.createNew(fdClass, 8));
        }

        System.out.println("\n\n## NEGATIVE EXAMPLES:");
        for (final NegativeExample nex : negativeExamplesList) {
            System.out.println("\n-------- ");
            System.out.println("## View: ");
            System.out.println(nex.view.toString());
            System.out.println("## Values: ");
            System.out.println(nex.getValues().toString());
        }

        // Assign negative examples to all available expert users
        List<Assignment> assignments = List.of();

        System.out.println("\n\n## Assign users to jobs");
        for (Iterator<ExpertUser> iterator = expertUserList.iterator(); iterator.hasNext();) {
            //for (ExpertUser expertUser : expertUserList) {
            ExpertUser expertUser = iterator.next();
            System.out.println("## Try to assign user: " + expertUser.toString());
            assignments = crowdSourcingDummyService.makeAssignment(expertUserList, negativeExamplesList);
            if (assignments.isEmpty()) {
                LOGGER.info("could not assign negative example to expert user!");
            }
            else {
                LOGGER.info("user assigned :)");

                // TODO: if assigned from parent, wrong user will removed!!!
                // remove assigned user
                iterator.remove();
                // For testing remove assigned negative example
                //negativeExamplesList.remove(0);
            }
        }

        // Simulate user decisions
        System.out.println("\n\n## Simulationg decision of one user");
        Decision decision = new Decision();
        // set decision state
        decision.setStatus(Decision.Status.REJECTED);
        // set decision reason
        List<PredefinedReason> predefinedReasonList = new ArrayList<>();
        predefinedReasonList.add(Decision.PredefinedReason.VALUE_MUST_BE_UNIQUE_IN_COLUMNS);
        List<ColumnReason<PredefinedReason>> reasons = new ArrayList<>();
        reasons.add(new ColumnReason<>(predefinedReasonList, "Name"));
        decision.setPredefinedReasons(reasons);

        // get an assignment for decision simulation
        if (!assignments.isEmpty()) {
            Assignment assignment = assignments.get(0);


            // TODO removed by me
            // assignment.getNegativeExample().setDecision(decision);
            // add idle user to list users which can be assigned
            expertUserList.add(assignment.expert);

            // get next negative example
            List<NegativeExample> updatedNegativeExampleList = negativeExampleBuilder.getNextNegativeExamples(
                    assignment.negativeExample,
                    decision,
                    null
            );

            // Update the list of negative examples
            for (NegativeExample nex : updatedNegativeExampleList) {
                // Add at the beginning of the list
                negativeExamplesList.add(0, nex);
            }
            updatedNegativeExampleList = null;

            System.out.println("\n\n## UPDATE NEGATIVE EXAMPLE LIST BASED ON DECISION");
            for (NegativeExample nex : negativeExamplesList) {
                System.out.println("\n-------- ");
                System.out.println("## View: ");
                System.out.println(nex.view.toString());
                System.out.println("## Values: ");
                System.out.println(nex.getValues().toString());
            }

            System.out.println("\n\n## Run rediscovery");
            List<String[]> updatedData = dataset.getRows();
            //System.out.println(negativeExamplesList.get(0).negativeExample().getValues().values());
            for (NegativeExample nex : negativeExamplesList) {
                //System.out.println(negEx.getValues().toString());
                Object[] objArray = nex.getValues().values().toArray();
                String[] strArray = Arrays.asList(objArray).toArray(new String[objArray.length]);
                updatedData.add(strArray);
            }


            //System.exit(0);

            discoveredFDs = fdDiscovering.execute(dataset.getHeader(), updatedData);

            System.exit(0);
            // Add further idle user so we have two users which accept jobs
            expertUserList.add(new ExpertUser("5"));

            // Try to assign further jobs
            System.out.println("\n\n## Again try to assign users to jobs");
            //for (ExpertUser expertUser : expertUserList) {
            for (Iterator<ExpertUser> iterator = expertUserList.iterator(); iterator.hasNext();) {

                ExpertUser expertUser = iterator.next();

                System.out.println("try to assign IDLE user: " + expertUser.toString());

                assignments = crowdSourcingDummyService.makeAssignment(expertUserList, negativeExamplesList);
                if (assignments == null) {
                    LOGGER.info("could not assign negative example to expert user!");
                } else {
                    LOGGER.info("user assigned :)");

                    // TODO: if assigned from parent, wrong user will removed!!!
                    // remove assigned user
                    iterator.remove();
                    // For testing remove assigned negative example
                    negativeExamplesList.remove(0);

                }
            }
        }
    }

}
