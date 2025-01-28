package de.uni.passau.server.crowdsourcing.serverdto;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ExpertUser implements Comparable<ExpertUser> {

    public final String id;
    private @Nullable Assignment currentAssignment = null;

    public ExpertUser(String id) {
        this.id = id;
    }

    public Assignment getCurrentAssignment() {
        return this.currentAssignment;
    }

    public void setCurrentAssignment(Assignment assignment) {
        this.currentAssignment = assignment;
    }

    public String toString() {
        return "ExpertUser [id=" + this.id + "]";
    }

    @Override
    public int compareTo(ExpertUser o) {
        return id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ExpertUser user && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
