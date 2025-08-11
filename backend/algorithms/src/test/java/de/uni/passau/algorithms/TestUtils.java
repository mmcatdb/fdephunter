package de.uni.passau.algorithms;

import java.util.ArrayList;
import java.util.List;

import de.uni.passau.core.model.ColumnSet;
import de.uni.passau.core.model.FdSet;
import de.uni.passau.core.model.MaxSet;
import de.uni.passau.core.model.MaxSets;
import de.uni.passau.core.model.FdSet.Fd;

public class TestUtils {

    /**
     * Pass one string for each class. The string will be split by whitespace and each element will be parsed as a column set.
     */
    public static MaxSets parseMaxSets(String ...classes) {
        final List<MaxSet> maxSets = new ArrayList<>();

        for (int forClass = 0; forClass < classes.length; forClass++) {
            final var maxSet = new MaxSet(forClass);
            maxSets.add(maxSet);

            final var elements = classes[forClass].trim().split("\\s+");

            for (String columns : elements)
                maxSet.addConfirmed(parseColumnSet(columns));
        }

        return new MaxSets(maxSets);
    }

    /**
     * Pass one continuous string of digits, e.g. "012" for a column set with columns { 0, 1, 2 }.
     */
    public static ColumnSet parseColumnSet(String columns) {
        final var columnSet = ColumnSet.fromIndexes();
        for (int i = 0; i < columns.length(); i++) {
            int column = Integer.parseInt(columns.substring(i, i + 1));
            columnSet.set(column);
        }
        return columnSet;
    }

    /**
     * Pass the total number of columns (the column names will be generated as "0", "1", ..., "n-1").
     * Then pass a list of functional dependencies in the format "lhs -> rhs", where lhs and rhs are passable {@link #parseColumnSet(String)}.
     */
    public static FdSet parseFds(int columnsCount, String ...fds) {
        final String[] columns = new String[columnsCount];
        for (int i = 0; i < columnsCount; i++)
            columns[i] = String.valueOf(i);

        final List<Fd> fdList = new ArrayList<>();

        for (String fd : fds) {
            final String[] parts = fd.split("->");
            if (parts.length != 2)
                throw new IllegalArgumentException("Invalid FD format: " + fd);

            final ColumnSet lhs = parseColumnSet(parts[0].trim());
            final ColumnSet rhs = parseColumnSet(parts[1].trim());

            fdList.add(new Fd(lhs, rhs));
        }

        return new FdSet(columns, fdList);
    }

}
