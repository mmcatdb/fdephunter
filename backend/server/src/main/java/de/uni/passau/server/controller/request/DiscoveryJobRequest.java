package de.uni.passau.server.controller.request;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;

public record DiscoveryJobRequest(
    String description,
    ApproachName approach,
    String dataset
) {}
