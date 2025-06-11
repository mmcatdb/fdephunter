package de.uni.passau.server.model.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Dataset")
public class DatasetNode {

    public static enum DatasetType {
        CSV, JSON, XML, LABELED_GRAPH, RELATIONAL, ARRAY, RDF;
    }

    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    public String getId() {
        return id;
    }

    @Property
    public String name;

    @Property
    public DatasetType type;

    @Property
    public String source;    // connection string or ...

    @Property
    public String kindName;

    @Property
    public Long columns;

    @Property
    public Long rows;

    @Property
    public Double size;

    @Property
    public Long fds;

    public DatasetNode(String name, DatasetType type, String source, String kindName, Long columns, Long rows, Double size, Long fds) {
        this.name = name;
        this.type = type;
        this.source = source;
        this.kindName = kindName;
        this.columns = columns;
        this.rows = rows;
        this.size = size;
        this.fds = fds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DatasetNode{");
        sb.append("id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", type=").append(type);
        sb.append(", source=").append(source);
        sb.append(", kindName=").append(kindName);
        sb.append(", columns=").append(columns);
        sb.append(", rows=").append(rows);
        sb.append(", size=").append(size);
        sb.append(", fds=").append(fds);
        sb.append('}');
        return sb.toString();
    }

}
