package de.uni.passau.server.controller;

import de.uni.passau.server.clientdto.Expert;
import de.uni.passau.server.clientdto.User;
import de.uni.passau.server.service.ExpertService;
import de.uni.passau.server.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ExpertController {

    @Autowired
    private ExpertService expertService;

    @Autowired
    private UserService userService;

    @GetMapping("/domain-experts/{expertId}")
    public Mono<Expert> getExpert(@PathVariable String expertId) {
        return expertService.findById(expertId).map(Expert::fromNodes);
    }

    @PostMapping("/domain-experts/{expertId}/accept")
    // dostaneme payload s duvody
    public Mono<Expert> acceptAssignment(@PathVariable String expertId) {
        return expertService.acceptAssignment(expertId).map(Expert::fromNodes);
    }

    @PostMapping("/domain-experts/{expertId}/reject")
    // dostaneme payload s duvody
    public Mono<Expert> rejectAssignment(@PathVariable String expertId) {
        return expertService.rejectAssignment(expertId).map(Expert::fromNodes);
    }

    @GetMapping("/users/experts")
    public Flux<User> getAllExpertUsers() {
        return userService.findAllExpertUsers().map(User::fromNodes);
    }

}
