package de.uni.passau.algorithms.exception;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ExtendMaxSetException extends AlgorithmException {

    protected ExtendMaxSetException(String name, @Nullable Serializable data, @Nullable Throwable cause) {
        super("extendMaxSet." + name, data, cause);
    }

    public static ExtendMaxSetException inner(Exception exception) {
        return exception instanceof ExtendMaxSetException extendMaxSetException
            ? extendMaxSetException
            : new ExtendMaxSetException("inner", exception.getMessage(), exception);
    }

}
