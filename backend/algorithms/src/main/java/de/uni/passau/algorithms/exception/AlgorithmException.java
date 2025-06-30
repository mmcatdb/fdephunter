package de.uni.passau.algorithms.exception;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni.passau.core.exception.NamedException;

/**
 * Base class for all exceptions in this package.
 */
public abstract class AlgorithmException extends NamedException {

    protected AlgorithmException(String name, @Nullable Serializable data, @Nullable Throwable cause) {
        super("algorithm." + name, data, cause);
    }

}
