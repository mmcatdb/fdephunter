package de.uni.passau.core.dataset;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVDataset implements Dataset {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVDataset.class);

    private @Nullable String[] header = null;
    private @Nullable List<String[]> rows = null;
    private @Nullable DatasetMetadata metadata = null;
    private boolean isLoaded = false;

    private final @Nullable File inputFile;
    private final boolean hasHeader;
    private final char separator;

    public CSVDataset(String filename, boolean hasHeader, char separator) {
        this.hasHeader = hasHeader;
        this.separator = separator;

        inputFile = new File(filename);
        if (!inputFile.isFile()) {
            LOGGER.error("CSV dataset file '{}' not found.", inputFile.getAbsolutePath());
            throw new IllegalArgumentException("CSV dataset file '" + inputFile.getAbsolutePath() + "' not found.");
        }
    }

    @Override public List<String[]> getRows() {
        return rows;
    }

    @Override public String[] getHeader() {
        return header;
    }

    @Override public boolean isLoaded() {
        return isLoaded;
    }

    @Override public void load() {
        try (final var filereader = new FileReader(inputFile)) {
            final var csvParser = new CSVParserBuilder().withSeparator(separator).build();
            final var csvReader = new CSVReaderBuilder(filereader).withCSVParser(csvParser).build();
            if (hasHeader)
                header = csvReader.readNext();

            rows = csvReader.readAll();
            isLoaded = true;

            if (rows.isEmpty())
                throw new IllegalStateException("CSV file is empty: " + inputFile.getAbsolutePath());

            if (!hasHeader) {
                // If there is no header, we create a default one. E.g., A, B, C, ...
                header = new String[rows.get(0).length];
                for (int i = 0; i < header.length; i++)
                    header[i] = String.valueOf((char) ('A' + i));
            }
        }
        catch (IOException | CsvException ex) {
            // throw ex;
            LOGGER.error("Reading CSV file", ex);
        }
    }

    @Override public void free() {
        LOGGER.debug("Freeing loaded CSV data");
        rows = null;
        header = null;
        isLoaded = false;
    }

    @Override public DatasetMetadata getMetadata() {
        if (!isLoaded)
            throw new IllegalStateException("Data not loaded");

        if (metadata == null)
            metadata = new CSVDatasetMetadata(inputFile.getAbsolutePath(), inputFile.getName(), hasHeader, inputFile.length(), rows.get(0).length, rows.size());

        return metadata;
    }

    public record CSVDatasetMetadata(
        String filepath,
        String filename,
        boolean hasHeader,
        long size,
        int numberOfColumns,
        int numberOfRows
    ) implements DatasetMetadata {}

}
