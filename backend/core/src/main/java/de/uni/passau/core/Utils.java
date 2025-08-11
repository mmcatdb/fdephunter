package de.uni.passau.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni.passau.core.model.ColumnSet;

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

    public static @Nullable String isHexString(String string, int length) {
        if (string.length() != length)
            return "String is not of expected length " + length + ", but " + string.length() + ".";

        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (!Character.isDigit(c) && (c < 'a' || c > 'f') && (c < 'A' || c > 'F'))
                return "Character '" + c + "' at position " + i + " is not a valid hex character.";
        }

        return null; // No errors.
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

    /**
     * Returns all subsets of set with given size.
     * Make sure the size is smaller than the size of the se - nobody is going to check that for you.
     */
    public static List<ColumnSet> getAllSubsetsWithSize(ColumnSet set, int size) {
        final List<ColumnSet> output = new ArrayList<>();

        final int[] allIndexes = set.toIndexes();

        final int[] indexes = IntStream.range(0, size).toArray();
        final int[] maxIndexes = IntStream.range(set.size() - size, set.size()).toArray();

        while (indexes[0] <= maxIndexes[0]) {
            final var subset = ColumnSet.fromIndexes();
            for (int i = 0; i < indexes.length; i++)
                subset.set(allIndexes[indexes[i]]);
            output.add(subset);

            int position = size - 1;
            indexes[position]++;

            if (indexes[position] <= maxIndexes[position])
                continue;

            while (position > 0 && indexes[position] >= maxIndexes[position])
                position--;

            indexes[position]++;

            while (position < size - 1) {
                position++;
                indexes[position] = indexes[position - 1] + 1;
            }
        }

        return output;
    }

}
