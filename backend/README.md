# FDepHunter Backend

## Installation

- Create Neo4j container:
```bash
docker run -d -e NEO4J_AUTH=neo4j/password -p 7474:7474 -p 7687:7687 --name=neo4j neo4j
```
- You can also mount a volume to persist the database:
```bash
docker run -d -e NEO4J_AUTH=neo4j/password -p 7474:7474 -p 7687:7687 --name=neo4j --volume=<some_path_in_your_filesystem>:/data neo4j
```

- Reset Neo4j database (if needed):
```bash
echo "MATCH (n) DETACH DELETE n" | docker exec -i neo4j cypher-shell -u neo4j -p password
```

- Start development server (with hot reload):
```bash
./dev.sh
```