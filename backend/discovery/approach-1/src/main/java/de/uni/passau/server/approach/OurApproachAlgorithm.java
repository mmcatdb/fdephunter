package de.uni.passau.server.approach;

import de.uni.passau.core.approach.ApproachMetadata;
import de.uni.passau.core.approach.FDInit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OurApproachAlgorithm {

    private static final String VIEW_NULL = "##VIEW_NULL"; // null value in negative examples TODO: define globally
    private static final String EMPTY = "";    // null value in positive examples (i.e. input data)

    private static final Logger LOGGER = LoggerFactory.getLogger(OurApproachAlgorithm.class);

    public List<FDInit> execute(String[] header, List<String[]> data) {
        List<FDInit> fds = discoverFDs(header, data);
        boolean hasHeader = true;

        // order fds by lhs size
        fds.sort((FDInit t1, FDInit t2) -> t1.lhs().size() - t2.lhs().size());

        final List<FDInit> filteredFDs = new ArrayList<>();
        // do not add fds if left side is super set of another fd with same right side
        for (FDInit fd : fds) {
            final boolean isSuperSet = filteredFDs.stream().anyMatch(fd2 ->
                fd.lhs().containsAll(fd2.lhs()) && fd.rhs().equals(fd2.rhs())
                    && !fd.lhs().equals(fd2.lhs())
            );

            if (!isSuperSet)
                filteredFDs.add(translateFD(fd, header));
        }


        StringBuilder sb = new StringBuilder();
        filteredFDs.forEach(fd -> {
            List<String> lhs = fd.lhs();
            for (int i = 0; i < lhs.size(); i++) {
                if (hasHeader) {
                    sb.append(lhs.get(i));
                    if (i < lhs.size() - 1) {
                        sb.append(",");
                    }
                }
                else {
                    sb.append(Integer.parseInt(lhs.get(i)) + 1);
                    if (i < lhs.size() - 1) {
                        sb.append(",");
                    }
                }
            }
            if (hasHeader) {
                sb.append("->").append(fd.rhs()).append("\n");
            }
            else {
                sb.append("->").append(Integer.parseInt(fd.rhs()) + 1).append("\n");
            }
        });

        System.out.println(sb);
        LOGGER.info("Total FDS: {}", filteredFDs.size());

        return filteredFDs;
//        return sb.toString();
    }

    private FDInit translateFD(FDInit fd, String[] header) {
        if (header == null)
            return fd;

        final List<String> lhsTranslate = fd.lhs().stream().map(column -> header[Integer.parseInt(column)]).toList();

        return new FDInit(lhsTranslate, header[Integer.parseInt(fd.rhs())]);
    }

    private boolean isUCC(List<String[]> reader, List<String> lhs) {
        List<List<String>> values = new ArrayList<>();
        for (String[] row : reader) {
            List<String> value = new ArrayList<>();
            for (String c : lhs) {
                value.add(row[Integer.parseInt(c)]);
            }
            values.add(value);
        }
        Set<List<String>> unique = new HashSet<>();
        for (List<String> v : values) {
            if (unique.contains(v)) {
                return false;
            }
            else {
                unique.add(v);
            }
        }
        return true;
    }

    private boolean isFD(List<String[]> data, List<String> lhs, String rhs) {
        if (isUCC(data, lhs)) {
            //return true; Calculating UCCs first should be more efficient but the funciton has a bug
        }
        int unskippedRows = 0;
        List<String> lhsIndex = new ArrayList<>();
        Map<String, List<String>> processedTuples = new HashMap<>();
        int mode = 2; // 0: skip, 1: null == null, 2: null != null

        final int rhsIndex = Integer.parseInt(rhs);
        final List<String> rhsValues = data.stream().map(row -> row[rhsIndex]).toList();

        final List<Integer> lhsIndices = lhs.stream().map(Integer::parseInt).toList();
        final List<String> lhsValues = data.stream().flatMap(row -> lhsIndices.stream().map(lhsColumn -> row[lhsColumn])).toList();

        for (final String[] row : data) {
            // Ignore empty fields (null values) on rhs
            String rhsValue = row[Integer.parseInt(rhs)];
            if (rhsValue.equals(VIEW_NULL) || (rhsValue.equals(EMPTY) && mode == 0))
                continue;

            if (rhsValue.equals(EMPTY)) {
                if (mode == 1) { // null == null
                    // do nothing
                }
                else if (mode == 2) { // null != null
                    // RHS is considered unique
                    // TODO: create unique rhs more efficiently
                    while (rhsValues.contains(rhsValue)) {
                        rhsValue += " ";
                    }
                    rhsValues.add(rhsValue);
                }
            }

            StringBuilder lhsValuesBuilder = new StringBuilder();
            boolean skip = false;
            for (String lhsColumn : lhs) {
                String lhsValue = row[Integer.parseInt(lhsColumn)];
                // Handle null values on lhs
                if (lhsValue.equals(VIEW_NULL) || (lhsValue.equals(EMPTY) && mode == 0)) {
                    skip = true;
                    break;
                }

                if (lhsValue.equals(EMPTY)) {
                    if (mode == 1) { // null == null
                        // do nothing
                    }
                    else if (mode == 2) { // null != null
                        // LHS is considered unique
                        // If LHS is unique, FD must hold
                        //skip = true;
                        //unskippedRows++;
                        //break;
                        // TODO: create unique lhs more efficiently
                        while (lhsValues.contains(rhsValue)) {
                            lhsValue += " ";
                        }
                        lhsValues.add(lhsValue);
                    }
                }

                int indexVal = lhsIndex.indexOf(lhsValue);
                if (indexVal == -1) {
                    lhsIndex.add(lhsValue);
                    indexVal = lhsIndex.size() - 1;
                }
                lhsValuesBuilder.append(indexVal).append(",");
            }

            // Ignore empty fields (null values) on lhs
            if (skip) {
                continue;
            }

            String lhsValuesStr = lhsValuesBuilder.substring(0, lhsValuesBuilder.length() - 1);
            List<String> tupleValues = processedTuples.get(lhsValuesStr);
            if (tupleValues == null) {
                tupleValues = new ArrayList<>();
                processedTuples.put(lhsValuesStr, tupleValues);
            }
            if (!tupleValues.contains(rhsValue) && !tupleValues.isEmpty()) {
                return false;
            }

            tupleValues.add(rhsValue);
            unskippedRows++;
        }

        return unskippedRows > 0;
    }

    private List<FDInit> discoverFDs(String[] header, List<String[]> data) {
        List<String> columns = new ArrayList<>();

        for (int i = 0; i < header.length; i++) {
            columns.add(String.valueOf(i));
        }

        List<List<String>> lhsCombinations = findAllCombinations(columns);
        List<FDInit> fds = new ArrayList<>();
        lhsCombinations.forEach(lhs ->
            columns.stream()
                .filter(rhs -> !lhs.contains(rhs))
                .filter(rhs -> isFD(data, lhs, rhs))
                .forEachOrdered(rhs -> fds.add(new FDInit(lhs, rhs))
            )
        );

        return fds;
    }

    private void generateCombinations(List<String> array, List<String> combinations, int start, List<List<String>> allCombinations) {
        for (int i = start; i < array.size(); i++) {
            combinations.add(array.get(i));
            allCombinations.add(new ArrayList<>(combinations));
            generateCombinations(array, combinations, i + 1, allCombinations);

            combinations.remove(combinations.size() - 1);
        }
    }

    private List<List<String>> findAllCombinations(List<String> array) {
        List<List<String>> allCombinations = new ArrayList<>();
        generateCombinations(array, new ArrayList<>(), 0, allCombinations);

        return allCombinations;
    }

    public ApproachMetadata getMetadata() {
        // WARN: INEFFICIENT!
        return new Approach1Metadata();
    }

}
