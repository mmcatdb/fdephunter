package de.uni.passau.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.uni.passau.core.model.ColumnSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class Utils {

    /**
     * @param n 0 or larger (up to 31)
     * @param k 0 or larger (up to 31)
     */
    public static int nChooseK(int n, int k) {
        if (k > n)
            return 0;

        return factorial(n) / (factorial(k) * factorial(n - k));
    }

    // Factorial of 0 is 1.
    private static IntList factorialCache = new IntArrayList(new int[] { 1 });

    /**
     * Don't use with numbers larger than 31!
     */
    public static int factorial(int x) {
        while (x >= factorialCache.size()) {
            final int next = factorialCache.size() * factorialCache.getInt(factorialCache.size() - 1);
            factorialCache.add(next);
        }

        return factorialCache.getInt(x);
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
        /** On index [n][k] contains the value of n choose k. Some elements might not be used (if the value for them isn't needed). */
        private final int[][] nChooseKCache;
        /** Indexed from 1 to n. The zeroth element is not used. */
        private final int[] maxIndexCache;
        public final int n;

        /**
         * @param n The total number of elements to choose from.
         */
        public CombinationIndexer(int n) {
            this.n = n;
            final int maxK = n;

            nChooseKCache = new int[n][];
            maxIndexCache = new int[maxK + 1];


            // The largest N is going to be n - 0 - 1, i.e., n - 1. The smallest N will be n - (n - 1) - 1, i.e., 0.
            for (int N = 0; N < n; N++) {
                this.nChooseKCache[N] = new int[maxK + 1];

                // The largest K is going to be k - 0, i.e., k. The smallest K will be k - (k - 1), i.e., 1. But we can also compute (N 0), just for simplicity.
                for (int K = 0; K <= maxK; K++)
                    this.nChooseKCache[N][K] = nChooseK(N, K);
            }

            for (int K = 1; K <= maxK; K++)
                this.maxIndexCache[K] = nChooseK(n, K) - 1;
        }

        /**
         * Returns the index (0-based) of the given combination in the lexicographically ordered list of all combinations of size k from n elements.
         * Based on {@link https://en.wikipedia.org/wiki/Combinatorial_number_system}, but adapted to our needs. The original algorithm sorts the combinations by their largest element, not the smallest, so it produces a little different ordering.
         * The column set is expected to include values from 0 to n - 1 (not all of them ofc).
         * I.e., don't use the specially-indexed column sets from max sets that omit the class column.
         */
        public int getIndex(ColumnSet c) {
            int sum = 0;
            final var indexes = c.toIndexes();
            final var k = indexes.length;
            final var cache = this.nChooseKCache;

            for (int i = 0; i < k; i++)
                sum += cache[n - indexes[i] - 1][k - i];

            return this.maxIndexCache[k] - sum;
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
                throw new IllegalArgumentException("k must be between 0 and n (inclusive), but is " + k + ".");

            return this.maxIndexCache[k] + 1;
        }
    }

}
