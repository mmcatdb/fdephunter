package de.uni.passau.server.controller.request;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;

public record RediscoveryJobRequest(
    String description,
    ApproachName approach
) {}
