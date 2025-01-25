package de.uni.passau.server.service;

import de.uni.passau.server.model.ExpertNode;
import de.uni.passau.server.model.RoleNode;
import de.uni.passau.server.model.RoleNode.RoleType;
import de.uni.passau.server.model.UserNode;
import de.uni.passau.server.repository.ExpertRepository;
import de.uni.passau.server.repository.ExpertRepository.ExpertNodeGroup;
import de.uni.passau.server.repository.RoleRepository;
import de.uni.passau.server.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpertRepository expertRepository;

    @Autowired
    private RoleRepository roleRepository;

    public Flux<UserNode> findAllExpertUsers() {
        return userRepository.findAllExpertUsers();
    }

    public Mono<UserNode> saveUser(UserNode user) {
        return userRepository.save(user);
    }

    // public Mono<UserNode> addRole(UserNode user, String role) {
    //     return userRepository.setRole(user.getId(), role);
    // }

    // public Mono<UserNode> saveUser(UserNode user, String role) {
    // return userRepository.save(user);
    // .then(
    //         userRepository.setRole(user.getId(), role)
    // );

    // AtomicReference<RoleNode> roleReference = new AtomicReference<>();
    // roleRepository.getExpert().subscribe(role -> {
    //     roleReference.set(role);
    // });
    // userRepository.setRole(expert.getId(),/*roleReference.get().getValue()*/ role);
    // return result;
    // }

    public Mono<UserNode> makeOwnerOfWorkflow(String userId, String workflowId) {
        return userRepository.saveIsOwner(userId, workflowId);
    }

    public Mono<ExpertNodeGroup> makeExpertOfWorkflow(String userId, String workflowId) {
        var newExpert = ExpertNode.createNew();
        return userRepository.isUserInWorkflow(userId, workflowId).filter(value -> !value)
            .switchIfEmpty(Mono.error(new RuntimeException("expert already exists")))
            .then(
                expertRepository.save(newExpert).flatMap(expert ->
                    userRepository.saveIsExpert(userId, expert.getId())
                        .switchIfEmpty(Mono.error(new RuntimeException("IS_EXPERT")))
                        .flatMap(user ->
                            expertRepository.saveInWorkflow(expert.getId(), workflowId)
                                .switchIfEmpty(Mono.error(new RuntimeException("IN_WORKFLOW")))
                                .then(Mono.just(new ExpertNodeGroup(expert, user, null)))
                        )
                )
            );
    }

    public Flux<RoleNode> initializeRoles() {
        RoleNode owner = RoleNode.createNew(RoleType.OWNER);
        RoleNode expert = RoleNode.createNew(RoleType.EXPERT);
        List<RoleNode> roles = new ArrayList<>();
        roles.add(owner);
        roles.add(expert);

        return roleRepository.saveAll(roles);
    }

    // public Mono<UserRepository.IExpert> findLastExpertAssignment(String expertId) {
    //     return userRepository.findLastExpertAssignment(expertId).cast(UserRepository.IExpert.class).switchIfEmpty(userRepository.findExpert(expertId));
    // }

}
