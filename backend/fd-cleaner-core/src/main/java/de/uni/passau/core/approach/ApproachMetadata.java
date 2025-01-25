package de.uni.passau.core.approach;

import de.uni.passau.core.approach.AbstractApproach.ApproachName;

public interface ApproachMetadata {

    public abstract ApproachName getName();

    public abstract String getLabel();

    public abstract String getAuthor();

}
