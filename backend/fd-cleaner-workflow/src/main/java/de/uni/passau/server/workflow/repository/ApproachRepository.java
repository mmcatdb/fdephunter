/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package de.uni.passau.server.workflow.repository;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.server.workflow.model.ApproachNode;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;

/**
 *
 * @author pavel.koupil
 */
public interface ApproachRepository extends ReactiveNeo4jRepository<ApproachNode, ApproachName> {

}
