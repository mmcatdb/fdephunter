package de.uni.passau.server.approach;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;
import de.uni.passau.core.approach.ApproachMetadata;

public class Approach1Metadata implements ApproachMetadata {

    @Override
    public final ApproachName getName() {
        return ApproachName.Approach1;
    }

    @Override
    public final String getLabel() {
        return "Approach 1";
    }

    @Override
    public String getAuthor() {
        return "Stefan Klessinger et al.";
    }

}
