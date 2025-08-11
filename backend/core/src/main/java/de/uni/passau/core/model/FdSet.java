package de.uni.passau.core.model;

import java.util.List;

/**
 * A set of all functional dependencies (FDs) in a dataset.
 * Used primarily as an overview for the user.
 */
public class FdSet {

    /** Names of the columns. They are expected to be unique. */
    public String[] columns;
    /**
     * For each column index i, there is a list of all column sets that when put on the lhs they form a functional dependency (with i on the rhs).
     * All numbers are indexes to the {@link #columns} array.
     */
    public List<Fd> fds;

    public FdSet(String[] columns, List<Fd> fds) {
        this.columns = columns;
        this.fds = fds;
    }

    public String toComparableString() {
        final var stringFds = fds.stream()
            .map(Fd::toComparableString)
            .sorted()
            .toList();

        final StringBuilder sb = new StringBuilder();

        for (final String column : columns)
            sb.append(column);

        for (final String fd : stringFds)
            sb.append("\n").append(fd);

        return sb.toString();
    }

    public record Fd(
        ColumnSet lhs,
        ColumnSet rhs
    ) {

        public String toComparableString() {
            final StringBuilder sb = new StringBuilder();

            for (final int index : lhs.toIndexes())
                sb.append(index);

            sb.append(" -> ");

            for (final int index : rhs.toIndexes())
                sb.append(index);

            return sb.toString();
        }

    }

}
