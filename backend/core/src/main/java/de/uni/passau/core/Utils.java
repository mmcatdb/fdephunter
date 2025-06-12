package de.uni.passau.core;

import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

public class Utils {

    private static Map<Integer, Integer> factorialCache = new TreeMap<>();

    /**
     * Don't use with numbers larger than 31!
     */
    public static int factorial(int x) {
        if (x <= 1)
            return 1;

        final @Nullable Integer cached = factorialCache.get(x);
        if (cached != null)
            return cached;

        final int result = x * factorial(x - 1);
        factorialCache.put(x, result);

        return result;
    }

}
