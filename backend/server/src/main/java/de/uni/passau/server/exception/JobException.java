package de.uni.passau.server.exception;

import java.io.Serializable;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni.passau.core.exception.NamedException;

public class JobException extends NamedException {

    private JobException(String name, @Nullable Serializable data) {
        super("job." + name, data, null);
    }

    public static JobException assignmentUndecided(UUID assignmentId) {
        return new JobException("assignmentUndecided", assignmentId);
    }

}
