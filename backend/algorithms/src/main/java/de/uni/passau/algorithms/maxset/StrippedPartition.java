package de.uni.passau.algorithms.maxset;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.longs.LongList;

public class StrippedPartition {

    public final int forClass;
    public final List<LongList> values = new ArrayList<>();

    public StrippedPartition(int forClass) {
        this.forClass = forClass;
    }

    public void addElement(LongList element) {
        values.add(element);
    }

    @Override public String toString() {
        String s = "sp(";
        for (LongList il : values)
            s += il.toString() + "-";

        return s + ")";
    }

}
