package de.uni.passau.algorithms.exception;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ComputeLatticeException extends AlgorithmException {

    protected ComputeLatticeException(String name, @Nullable Serializable data, @Nullable Throwable cause) {
        super("computeLattice." + name, data, cause);
    }

    public static ComputeLatticeException inner(Exception exception) {
        return exception instanceof ComputeLatticeException computeLatticeException
            ? computeLatticeException
            : new ComputeLatticeException("inner", exception.getMessage(), exception);
    }

}
