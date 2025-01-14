package de.uni.passau.server.workflow.service;


import de.uni.passau.server.workflow.model.ExpertNode.ExpertState;
import de.uni.passau.server.workflow.repository.ExpertRepository;
import de.uni.passau.server.workflow.repository.ExpertRepository.ExpertNodeGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ExpertService {
    
    @Autowired
    private ExpertRepository expertRepository;

    public Flux<ExpertNodeGroup> findAllIdleInWorkflow(String workflowId) {
        return expertRepository.findAllIdleInWorkflow(workflowId);
    }

    public Flux<ExpertNodeGroup> findAllInWorkflow(String workflowId) {
        return expertRepository.findAllInWorkflow(workflowId);
    }

    public Mono<ExpertNodeGroup> findById(String expertId) {
        return expertRepository.findGroupById(expertId);
    }

    public Mono<ExpertNodeGroup> acceptAssignment(String expertId) {
        // return expertRepository.findById(expertId).flatMap(expert -> {
        //     expert.setState(ExpertState.IDLE);
        //     return expertRepository.save(expert);
        // });
        return expertRepository.setState(expertId, ExpertState.IDLE);
    }

    public Mono<ExpertNodeGroup> rejectAssignment(String expertId) {
        // return expertRepository.findById(expertId).flatMap(expert -> {
        //     expert.setState(ExpertState.UNSUBSCRIBED);
        //     return expertRepository.save(expert);
        // });
        return expertRepository.setState(expertId, ExpertState.UNSUBSCRIBED);
    }

}
