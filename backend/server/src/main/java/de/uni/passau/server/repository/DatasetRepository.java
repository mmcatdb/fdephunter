package de.uni.passau.server.repository;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import de.uni.passau.server.model.DatasetEntity;
import de.uni.passau.server.model.DatasetEntity.DatasetSettings;

public interface DatasetRepository extends MongoRepository<DatasetEntity, UUID> {

    boolean existsByName(String name);

    @Query(sort = "{ 'id' : -1 }")
    @Nullable DatasetEntity findFirstBySettingsAndSourceAndOriginalName(DatasetSettings settings, String source, String originalName);

}
