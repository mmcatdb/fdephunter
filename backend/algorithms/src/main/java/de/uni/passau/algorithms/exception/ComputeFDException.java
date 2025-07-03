package de.uni.passau.algorithms.exception;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ComputeFDException extends AlgorithmException {

    protected ComputeFDException(String name, @Nullable Serializable data, @Nullable Throwable cause) {
        super("computeFD." + name, data, cause);
    }

    public static ComputeFDException inner(Exception exception) {
        return exception instanceof ComputeFDException computeFDException
            ? computeFDException
            : new ComputeFDException("inner", exception.getMessage(), exception);
    }

}
