package de.uni.passau.algorithms.exception;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ComputeMaxSetException extends AlgorithmException {

    protected ComputeMaxSetException(String name, @Nullable Serializable data, @Nullable Throwable cause) {
        super("computeMaxSet." + name, data, cause);
    }

    public static ComputeMaxSetException inner(Exception exception) {
        return exception instanceof ComputeMaxSetException computeMaxSetException
            ? computeMaxSetException
            : new ComputeMaxSetException("inner", exception.getMessage(), exception);
    }

}
