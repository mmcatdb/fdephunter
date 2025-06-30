package de.uni.passau.algorithms.exception;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public class AdjustMaxSetException extends AlgorithmException {

    protected AdjustMaxSetException(String name, @Nullable Serializable data, @Nullable Throwable cause) {
        super("adjustMaxSet." + name, data, cause);
    }

    public static AdjustMaxSetException inner(Exception exception) {
        return exception instanceof AdjustMaxSetException adjustMaxSetException
            ? adjustMaxSetException
            : new AdjustMaxSetException("inner", exception.getMessage(), exception);
    }

}
