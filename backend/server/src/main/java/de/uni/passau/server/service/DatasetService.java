package de.uni.passau.server.service;

import java.io.Serializable;
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

import de.uni.passau.core.Utils;
import de.uni.passau.core.dataset.CSVDataset;
import de.uni.passau.core.dataset.Dataset;
import de.uni.passau.server.Configuration.ServerProperties;
import de.uni.passau.server.model.DatasetEntity;
import de.uni.passau.server.model.DatasetEntity.CsvSettings;
import de.uni.passau.server.model.DatasetEntity.DatasetSettings;
import de.uni.passau.server.repository.DatasetRepository;

@Service
public class DatasetService {

    /** Directory for uploaded datasets. Will be placed to the dataset directory. */
    private static final String UPLOADS_DIRECTORY = "uploads";

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private ServerProperties server;

    public Dataset getLoadedDatasetById(UUID id) {
        final var entity = datasetRepository.findById(id).get();
        final var dataset = getSpecificDataset(entity);
        if (!dataset.isLoaded())
            dataset.load();

        return dataset;
    }

    private Dataset getSpecificDataset(DatasetEntity entity) {
        if (entity.settings instanceof final CsvSettings csvSettings)
            return new CSVDataset(entity.source, csvSettings.hasHeader(), csvSettings.separator());

        throw new UnsupportedOperationException("Dataset type " + entity.settings.getClass().getName() + " is not supported yet.");

        // NICE_TO_HAVE Change to switch on class once we have a better java version.
        /*
        return switch (entity.settings.getType()) {
            // case ARRAY -> throw new UnsupportedOperationException(DatasetType.ARRAY + " dataset not supported yet.");
            case CSV -> new CSVDataset(entity.source, true);
            // case JSON -> throw new UnsupportedOperationException(DatasetType.JSON + " dataset not supported yet.");
            // case LABELED_GRAPH -> throw new UnsupportedOperationException(DatasetType.LABELED_GRAPH + " dataset not supported yet.");
            // case RDF -> throw new UnsupportedOperationException(DatasetType.RDF + " dataset not supported yet.");
            // case RELATIONAL -> throw new UnsupportedOperationException(DatasetType.RELATIONAL + " dataset not supported yet.");
            // case XML -> throw new UnsupportedOperationException(DatasetType.XML + " dataset not supported yet.");
        };
        */
    }

    public DatasetEntity createDataset(DatasetSettings settings, String uniqueName, String filename) {
        final var source = Paths.get(server.datasetDirectory(), filename).toString();
        // We expect no collisions here, so we can use the uniqueName as is.
        final var entity = DatasetEntity.create(settings, source, uniqueName, uniqueName);

        try {
            getSpecificDataset(entity);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Failed to create dataset from the provided settings and source.", e);
        }

        return datasetRepository.save(entity);
    }

    public record CsvDatasetInit(
        FileResponse file,
        boolean hasHeader,
        char separator
    ) implements Serializable {}

    public DatasetEntity createDataset(CsvDatasetInit init) {
        // This is just to make sure no one tries to upload a path to some other file.
        final @Nullable String hashError = Utils.isHexString(init.file.hash, HASH_LENGTH);
        if (hashError != null)
            throw new IllegalArgumentException("Invalid file hash: " + hashError);

        final String name = getDatasetName(init.file.originalName);
        final String source = getFilePath(init.file.hash).toString();
        final var settings = new CsvSettings(init.hasHeader, init.separator);

        // However, we want to ensure that the the dataset in the database is also unique.
        final @Nullable DatasetEntity existing = datasetRepository.findFirstBySettingsAndSourceAndOriginalName(settings, source, name);
        // We return the existing dataset only if it's identical to the one we are trying to create.
        if (existing != null)
            return existing;

        // If a dataset exists with the same content, it will be stored in the same location.
        // However, now we need to make the name unique because it would be really confusing for the user.
        final String uniqueName = makeDatasetNameUnique(name);

        final var dataset = DatasetEntity.create(settings, source, uniqueName, name);

        return datasetRepository.save(dataset);
    }

    private String getDatasetName(String filename) {
        // Just strip the extension, if any. The path should be already stripped of directories during the file upload.
        final int dotPosition = filename.lastIndexOf('.');
        if (dotPosition == -1 || dotPosition == 0) // Dot on the first position is just hidden file, so we don't care.
            return filename;

        return filename.substring(0, dotPosition);
    }

    private String makeDatasetNameUnique(String name) {
        if (!datasetRepository.existsByName(name))
            // No collision, so we can use the name as is.
            return name;

        // We are trying to find all names in the form of "name-1", "name-2", etc. We will extract the numbers and find the first one that isn't used.
        final String prefix = name + "-";

        final var usedNumbers = datasetRepository.findAll().stream()
            .map(dataset -> dataset.name)
            .map(usedName -> {
                System.out.println("Checking name: " + usedName);

                // Yes, we can filter this in the database, but let's just don't care for now.
                if (!usedName.startsWith(prefix))
                    return null;
                final String numberPart = usedName.substring(prefix.length());

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
                return prefix + i;
            }
        }

        throw new RuntimeException("This should never happen.");
    }

    private final static Pattern onlyDigitsRegex = Pattern.compile("^\\d+$");

    public record FileResponse(
        /**
         * It would be more convenient to use the full source. However, we don't want anyone to mess with this value since that's an internal part of the server.
         * Hash is something we can easily validate.
         */
        String hash,
        String originalName
    ) implements Serializable {}

    public FileResponse uploadFile(MultipartFile file) {
        final String filename = getFilename(file);
        // By computing the hash, we ensure that the file is not uploaded twice, because we will use the hash in the path to the stored file.
        final String hash = computeFileHash(file);

        saveFileData(file, getFilePath(hash));

        return new FileResponse(hash, filename);
    }

    private String getFilename(MultipartFile file) {
        // Start with some default name.
        String name = "dataset";

        final var filename = file.getOriginalFilename();
        if (filename != null)
            name = Paths.get(filename).getFileName().toString();

        return name;
    }

    private static int HASH_LENGTH = 64; // SHA-256 produces 64 hex characters.

    private String computeFileHash(MultipartFile file) {
        try {
            final byte[] hash = MessageDigest.getInstance("SHA-256").digest(file.getBytes());
            return Utils.bytesToHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute file hash", e);
        }
    }

    private Path getFilePath(String hash) {
        return Paths.get(server.datasetDirectory(), UPLOADS_DIRECTORY, hash);
    }

    private void saveFileData(MultipartFile file, Path path) {
        try {
            file.transferTo(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save dataset file", e);
        }
    }

}
