package de.uni.passau.server.service;

import de.uni.passau.core.Utils;
import de.uni.passau.core.dataset.CSVDataset;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.server.Configuration.ServerProperties;
import de.uni.passau.server.model.DatasetEntity;
import de.uni.passau.server.model.DatasetEntity.DatasetType;
import de.uni.passau.server.repository.DatasetRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DatasetService {

    /** Directory for uploaded datasets. Will be placed to the dataset directory. */
    private static final String UPLOADS_DIRECTORY = "uploads";

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private ServerProperties server;

    public DatasetEntity createDataset(DatasetType type, String name, String fileName) {
        // TODO Test if file exists here.
        final var source = Paths.get(server.datasetDirectory(), fileName).toString();
        // We expect no collisions here, so we can use the name as is.
        final var dataset = DatasetEntity.create(type, source, name, name);
        return datasetRepository.save(dataset);
    }

    public Dataset getLoadedDatasetById(UUID id) {
        final var entity = datasetRepository.findById(id).get();
        final var dataset = getSpecificDataset(entity);
        if (!dataset.isLoaded())
            dataset.load();

        return dataset;
    }

    private Dataset getSpecificDataset(DatasetEntity entity) {
        return switch (entity.type) {
            case ARRAY -> throw new UnsupportedOperationException(DatasetType.ARRAY + " dataset not supported yet.");
            case CSV -> new CSVDataset(entity.source, true);
            case JSON -> throw new UnsupportedOperationException(DatasetType.JSON + " dataset not supported yet.");
            case LABELED_GRAPH -> throw new UnsupportedOperationException(DatasetType.LABELED_GRAPH + " dataset not supported yet.");
            case RDF -> throw new UnsupportedOperationException(DatasetType.RDF + " dataset not supported yet.");
            case RELATIONAL -> throw new UnsupportedOperationException(DatasetType.RELATIONAL + " dataset not supported yet.");
            case XML -> throw new UnsupportedOperationException(DatasetType.XML + " dataset not supported yet.");
        };
    }

    public DatasetEntity uploadDataset(MultipartFile file) {
        final String name = getDatasetName(file);
        // By computing the hash, we ensure that the file is not uploaded twice, because we will use the hash as the filename.
        final String hash = computeFileHash(file);
        final String source = Paths.get(server.datasetDirectory(), UPLOADS_DIRECTORY, hash).toString();

        // However, we want to ensure that the the dataset in the database is also unique.
        final @Nullable DatasetEntity existing = datasetRepository.findFirstByTypeAndSourceAndOriginalName(DatasetType.CSV, source, name);
        // We return the existing dataset only if it's identical to the one we are trying to create.
        if (existing != null)
            return existing;

        // If a dataset exists with the same content, it will be stored in the same location.
        // However, now we need to make the name unique because it would be really confusing for the user.
        final String uniqueName = makeDatasetNameUnique(name);

        final var dataset = DatasetEntity.create(DatasetType.CSV, source, uniqueName, name);

        saveFileData(file, dataset);

        return datasetRepository.save(dataset);
    }

    private String getDatasetName(MultipartFile file) {
        // Start with some default name.
        String name = "dataset";

        final var filename = file.getOriginalFilename();
        if (filename != null)
            name = Paths.get(filename).getFileName().toString();

        return name;
    }

    private String makeDatasetNameUnique(String name) {
        if (!datasetRepository.existsByName(name))
            // No collision, so we can use the name as is.
            return name;

        // We are trying to find all names in the form of "name-1.xyz", "name-2.xyz", etc. We will extract the numbers and find the first one that isn't used.
        final int dotPosition = name.lastIndexOf('.');
        final String baseName = dotPosition == -1 ? name : name.substring(0, dotPosition);
        final String suffix = dotPosition == -1 ? "" : name.substring(dotPosition);
        final String prefix = baseName + "-";

        System.out.println("Prefix: " + prefix + ", Suffix: " + suffix);

        final var usedNumbers = datasetRepository.findAll().stream()
            .map(dataset -> dataset.name)
            .map(usedName -> {
                System.out.println("Checking name: " + usedName);

                // Yes, we can filter this in the database, but let's just don't care for now.
                if (!usedName.startsWith(prefix))
                    return null;
                final String afterDash = usedName.substring(prefix.length());

                System.out.println("After dash: " + afterDash);

                if (!afterDash.endsWith(suffix))
                    return null;
                final String numberPart = afterDash.substring(0, afterDash.length() - suffix.length());

                System.out.println("Number part: " + numberPart);

                // We don't want any funny business like 0x123 or 123e456.
                final var matcher = onlyDigitsRegex.matcher(numberPart);
                return matcher.find() ? Integer.parseInt(matcher.group()) : null;
            })
            .filter(number -> number != null)
            .collect(Collectors.toSet());

        for (int i = 1; i <= usedNumbers.size() + 1; i++) {
            System.out.println("Using number: " + i);
            if (!usedNumbers.contains(i)) {
                System.out.println("Found unused number: " + i);
                return prefix + i + suffix;
            }
        }

        throw new RuntimeException("This should never happen.");
    }

    private static Pattern onlyDigitsRegex = Pattern.compile("^\\d+$");

    private String computeFileHash(MultipartFile file) {
        try {
            final byte[] hash = MessageDigest.getInstance("SHA-256").digest(file.getBytes());
            return Utils.bytesToHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute file hash", e);
        }
    }

    private void saveFileData(MultipartFile file, DatasetEntity dataset) {
        try {
            final Path path = Paths.get(dataset.source);
            file.transferTo(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save dataset file", e);
        }
    }
}
