package de.uni.passau.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni.passau.core.model.ColumnSet;

public class Utils {

    /** Max n for which n choose k for all possible values of k fits into integer (something above 2e9). */
    private static final int MAX_N = 33;

    /** On index [n][k] contains the value of n choose k. Some elements might not be used (if the value for them isn't needed). */
    private static int[][] nChooseKCache = new int[MAX_N + 1][];

    static {
        nChooseKCache[0] = new int[MAX_N + 1];
        nChooseKCache[0][0] = 1; // 0 choose 0 = 1
        for (int k = 1; k <= MAX_N; k++)
            nChooseKCache[0][k] = 0; // 0 choose k = 0 for k > 0

        for (int n = 1; n <= MAX_N; n++) {
            nChooseKCache[n] = new int[MAX_N + 1];
            nChooseKCache[n][0] = 1; // n choose 0 = 1
            nChooseKCache[n][n] = 1; // n choose n = 1

            for (int k = 1; k < n; k++)
                nChooseKCache[n][k] = nChooseKCache[n - 1][k - 1] + nChooseKCache[n - 1][k];
            for (int k = n + 1; k <= MAX_N; k++)
                nChooseKCache[n][k] = 0; // n choose k = 0 for k > n
        }
    }

    /**
     * @param n from 0 to 33 (both inclusive)
     * @param k from 0 to 33 (both inclusive)
     */
    public static int nChooseK(int n, int k) {
        return nChooseKCache[n][k];
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
            final var subset = ColumnSet.empty();
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

    /**
     * A cached computer of combination indexes.
     */
    public static class CombinationIndexer {
        public final int n;

        private CombinationIndexer(int n) {
            this.n = n;
        }

        /**
         * @param n The total number of elements to choose from.
         */
        public static CombinationIndexer create(int n) {
            if (n < 1 || n > MAX_N)
                throw new IllegalArgumentException("n must be between 1 and " + MAX_N + " (both inclusive), but is " + n + ".");

            return new CombinationIndexer(n);
        }

        /**
         * Returns the index (0-based) of the given combination in the lexicographically ordered list of all combinations of size k from n elements.
         * Based on {@link https://en.wikipedia.org/wiki/Combinatorial_number_system}, but adapted to our needs. The original algorithm sorts the combinations by their largest element, not the smallest, so it produces a little different ordering.
         * The column set is expected to include values from 0 to n - 1 (not all of them ofc).
         * I.e., don't use the specially-indexed column sets from max sets that omit the class column.
         */
        public int getIndex(ColumnSet c) {
            // At the end, we would have subtracted 1 from the nChooseK value, so we start with 1.
            int sum = 1;
            final var indexes = c.toIndexes();
            final var k = indexes.length;

            for (int i = 0; i < k; i++)
                sum += nChooseK(n - indexes[i] - 1, k - i);

            return nChooseK(n, k) - sum;
        }

        // For example, lets say n = 5 and k = 3. Then the combinations with their indexes are:
        // 0: {0,1,2}
        // 1: {0,1,3}
        // 2: {0,1,4}
        // 3: {0,2,3}
        // 4: {0,2,4}
        // 5: {0,3,4}
        // 6: {1,2,3}
        // 7: {1,2,4}
        // 8: {1,3,4}
        // 9: {2,3,4}

        /** Returns the number of combinations (without repetition) of <code>k</code> elements choosen from <code>n</code> elements (i.e., n choose k). */
        public int getNumberOfCombinations(int k) {
            if (k < 1 || k > n)
                throw new IllegalArgumentException("k must be between 1 and n (both inclusive), but is " + k + ".");

            return nChooseK(n, k);
        }
    }

}
