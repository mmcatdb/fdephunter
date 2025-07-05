package de.uni.passau.server;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.server.Configuration.DatabaseProperties;

import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter;
import org.springframework.lang.NonNull;

@Configuration
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Autowired
    private DatabaseProperties database;

    @Override protected @NonNull String getDatabaseName() {
        return database.database();
    }

    @Override public @NonNull MongoClient mongoClient() {
        final var connectionString = new StringBuilder()
            .append("mongodb://")
            .append(database.username())
            .append(":")
            .append(database.password())
            .append("@")
            .append(database.host())
            .append(":")
            .append(database.port())
            .toString();

        final var mongoSettings = MongoClientSettings.builder()
            .applyConnectionString(new com.mongodb.ConnectionString(connectionString))
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .build();

        return MongoClients.create(mongoSettings);
    }

    @WritingConverter
    public class ColumnSetWriteConverter implements Converter<ColumnSet, String> {
        @Override public String convert(@NonNull ColumnSet source) {
            return source.toBase64String();
        }
    }

    @ReadingConverter
    public class ColumnSetReadConverter implements Converter<String, ColumnSet> {
        @Override public ColumnSet convert(@NonNull String source) {
            return ColumnSet.fromBase64String(source);
        }
    }

    @Override protected void configureConverters(@NonNull MongoConverterConfigurationAdapter adapter) {
        adapter.registerConverter(new ColumnSetWriteConverter());
        adapter.registerConverter(new ColumnSetReadConverter());
    }

}
