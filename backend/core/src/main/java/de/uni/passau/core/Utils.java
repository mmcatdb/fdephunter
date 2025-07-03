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

    public static String bytesToHexString(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();

        // We do this in reverse so that the first byte is the most significant one.
        // So, when translated from hex to binary, output 1000 0000 will correspond to a set { 7 }.
        for (int i = bytes.length - 1; i >= 0; i--) {
            sb.append(Character.forDigit((bytes[i] >> 4) & 0xF, 16));
            sb.append(Character.forDigit(bytes[i] & 0xF, 16));
        }

        return sb.toString();
    }

    public static byte[] hexStringToBytes(String string) {
        // The string array is expected to be of even length, so we can divide it by 2.
        final int length = string.length() / 2;
        final byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {
            int firstDigit = Character.digit(string.charAt(2 * i), 16);
            int secondDigit = Character.digit(string.charAt(2 * i + 1), 16);
            // Again, reading in reverse order.
            bytes[length - i - 1] = (byte) ((firstDigit << 4) | secondDigit);
        }

        return bytes;
    }

}
