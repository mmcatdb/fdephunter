package de.uni.passau.server.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Dataset")
public class DatasetNode {

    public static enum DatasetType {
        CSV, JSON, XML, LABELED_GRAPH, RELATIONAL, ARRAY, RDF;
    }

    @Id
    @GeneratedValue
    private Long id;

    @Property
    private String name;

    @Property
    private DatasetType type;

    @Property
    private String source;    // connection string or ...

    @Property
    private String kindName;

    @Property
    private Long columns;

    @Property
    private Long rows;

    @Property
    private Double size;

    @Property
    private Long fds;

    public DatasetNode() {
    }

    public DatasetNode(Long id, String name, DatasetType type, String source, String kindName, Long columns, Long rows, Double size, Long fds) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.source = source;
        this.kindName = kindName;
        this.columns = columns;
        this.rows = rows;
        this.size = size;
        this.fds = fds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatasetType getType() {
        return type;
    }

    public void setType(DatasetType type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getKindName() {
        return kindName;
    }

    public void setKindName(String kindName) {
        this.kindName = kindName;
    }

    public Long getColumns() {
        return columns;
    }

    public void setColumns(Long columns) {
        this.columns = columns;
    }

    public Long getRows() {
        return rows;
    }

    public void setRows(Long rows) {
        this.rows = rows;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public Long getFds() {
        return fds;
    }

    public void setFds(Long fds) {
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
