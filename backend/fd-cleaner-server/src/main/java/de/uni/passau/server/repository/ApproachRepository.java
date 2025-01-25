package de.uni.passau.server.repository;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.server.model.ApproachNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;

public interface ApproachRepository extends ReactiveNeo4jRepository<ApproachNode, ApproachName> {

}
