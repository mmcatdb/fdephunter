package de.uni.passau.algorithms.exception;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ComputeARException extends AlgorithmException {

    protected ComputeARException(String name, @Nullable Serializable data, @Nullable Throwable cause) {
        super("computeAR." + name, data, cause);
    }

    public static ComputeARException inner(Exception exception) {
        return exception instanceof ComputeARException computeARException
            ? computeARException
            : new ComputeARException("inner", exception.getMessage(), exception);
    }

}
