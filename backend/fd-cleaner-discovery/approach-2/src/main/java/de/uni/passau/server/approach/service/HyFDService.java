/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.approach.service;

import de.uni.passau.core.approach.ApproachMetadata;
import de.uni.passau.core.approach.FDInit;
import de.uni.passau.server.approach.HyFDMetadata;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.result_receiver.OmniscientResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import de.metanome.algorithm_integration.results.Result;
import de.metanome.algorithms.hyfd.HyFDModified;
import de.metanome.algorithms.run.ResultCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author pavel.koupil
 */
@Service
public class HyFDService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HyFDService.class);

    private final HyFDModified algorithm = new HyFDModified();

    public List<FDInit> execute(String[] header, List<String[]> data) {

        final Boolean null_equals_null = true;

        //final Character csv_separator = ',';
        //final Boolean csv_has_header = true;

        algorithm.setOurData(header, data);

        // ---- Setup the result receiver
        OmniscientResultReceiver resultReceiver = null;
        try {
            resultReceiver = new ResultCache("hyfd-result", null);
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot create ResultCache.", e);
        }

        algorithm.setResultReceiver(resultReceiver);

        // --- Setup algorithm parameter
        try {

            algorithm.setBooleanConfigurationValue("NULL_EQUALS_NULL", null_equals_null);
        } catch (AlgorithmConfigurationException e) {
            System.out.println("ERROR: " + e.toString());
        }

        // ---- CSV file input
        /*
        ConfigurationSettingFileInput setting = new ConfigurationSettingFileInput(
                "input_csv_file_path",
                true,
                csv_separator,
                '"',
                '\\',
                false, // inputFileStrictQuotes
                true, // inputFileIgnoreLeadingWhiteSpace
                0, // inputFileSkipLines
                csv_has_header, // inputFileHasHeader
                false, // inputFileSkipDifferingLines,
                "" //inputFileNullString
        );
        */

        // Execute the algorithm
        try {
            algorithm.execute();
        } catch (Exception e) {
            System.out.println("ERROR: Algorithm crashed, " + e.toString());
        }

        // TODO: process the result
        // Print the results
        List<FDInit> filteredFDs = new ArrayList<>();

        if (resultReceiver != null) {
//            System.out.println("\n\n## RESULTS ##");
            int index = 0;
            for (Result result : ((ResultCache) resultReceiver).fetchNewResults()) {

                List<String> lhs = new ArrayList<>();

                var xxx = (FunctionalDependency) result;
                xxx.getDeterminant().getColumnIdentifiers().forEach(varx -> {
                    lhs.add(varx.getColumnIdentifier());
//                    System.out.println(varx.getColumnIdentifier());
                });
                String rhs = xxx.getDependant().getColumnIdentifier();
//                System.out.println(xxx.toString());
//                System.out.println(result.toString());

                filteredFDs.add(new FDInit(lhs, rhs));
            }
        }

//        LOGGER.info("Total FDS: {}", filteredFDs.size());

//        StringBuilder sb = new StringBuilder();
//        filteredFDs.forEach(fd -> {
//            List<String> lhs = fd.getFirst();
//            for (int i = 0; i < lhs.size(); i++) {
//                sb.append(lhs.get(i));
//                if (i < lhs.size() - 1) {
//                    sb.append(",");
//                }
//            }
//            sb.append("->").append(fd.getSecond()).append("\n");
//        });
//        System.out.println(sb);

        return filteredFDs;
    }

    public ApproachMetadata getMetadata() {
        // TODO: INEFFICIENT
        return new HyFDMetadata();
    }

}
