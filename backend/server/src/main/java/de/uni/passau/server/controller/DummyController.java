package de.uni.passau.server.controller;

import de.uni.passau.core.graph.Vertex;
import de.uni.passau.core.nex.Decision;
import de.uni.passau.server.function.FunctionMetadata;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class DummyController {

    @GetMapping("/dummy")
    public String dummy() {
        return "{\"message\": \"Dummy controller works!\"}";
    }

    @GetMapping("/dummy/weight-functions")
    public List<FunctionMetadata> getWeightFunctions() {
        return new ArrayList<>();
    }

    @GetMapping("/dummy/elimination/pick-next/job-id/{jobId}/last")
    public Vertex pickNext(@PathVariable String jobId) {
        return new Vertex("string");
    }

    @GetMapping("/dummy/elimination/pick-next/job-id/{jobId}/iteration/{iteration}/all")
    public List<Vertex> getPickNextPlan(@PathVariable String jobId, @PathVariable String iteration) {
        return new ArrayList<>();
    }

    @GetMapping("/dummy/elimination/examples/job-id/{jobId}/last")
    public List<String[]> getPositiveNegativeTuples(@PathVariable String jobId) {
        return new ArrayList<>();
    }

    @GetMapping("/dummy/elimination/decisions/job-id/{jobId}/iteration/{iteration}/vertex/{vertexId}/all")
    public List<Decision> getDecisions(@PathVariable String jobId, @PathVariable String iteration, @PathVariable String vertexId) {
        return new ArrayList<>();
    }

    @GetMapping("/dummy/elimination/decisions/job-id/{jobId}/iteration/{iteration}/all")
    public List<Decision> getDecisions(@PathVariable String jobId, @PathVariable String iteration) {
        return new ArrayList<>();
    }

}
