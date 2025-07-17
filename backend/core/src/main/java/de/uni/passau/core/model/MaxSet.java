package de.uni.passau.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = MaxSet.Serializer.class)
@JsonDeserialize(using = MaxSet.Deserializer.class)
public class MaxSet implements Cloneable {

    /** Index of the RHS column. */
    public final int forClass;
    /** Elements that are confirmed to be in the max set (at least in the current version of max set). */
    private final Set<ColumnSet> confirmeds = new HashSet<>();
    /** Elements that are speculatively added to the max set, but might be removed later (or moved to the confirmed category). */
    private final Set<ColumnSet> candidates = new HashSet<>();
    /** If true, the algorithm is finished (we have found the final set of functional dependencies). */
    private boolean isFinished = false;

    private MaxSet(int forClass, Iterable<ColumnSet> confirmeds, Iterable<ColumnSet> candidates, boolean isFinished) {
        this.forClass = forClass;

        for (final ColumnSet confirmed : confirmeds)
            this.confirmeds.add(confirmed);

        for (final ColumnSet candidate : candidates)
            this.candidates.add(candidate);

        this.isFinished = isFinished;
    }

    public MaxSet(int forClass) {
        this(forClass, List.of());
    }

    /** Mostly for tests. */
    public MaxSet(int forClass, Iterable<ColumnSet> confirmeds) {
        this(forClass, confirmeds, List.of(), false);
    }

    @Override public MaxSet clone() {
        return new MaxSet(forClass, confirmeds, candidates, isFinished);
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished() {
        this.isFinished = true;
    }

    public Iterable<ColumnSet> confirmedElements() {
        return confirmeds;
    }

    public int confirmedCount() {
        return confirmeds.size();
    }

    public int candidateCount() {
        return candidates.size();
    }

    public Stream<ColumnSet> elements() {
        return Stream.concat(
            confirmeds.stream(),
            candidates.stream()
        );
    }

    public boolean hasConfirmed(ColumnSet set) {
        return confirmeds.contains(set);
    }

    public boolean hasCandidate(ColumnSet set) {
        return candidates.contains(set);
    }

    // TODO these functions are not optimal. The problem is that it's not easy to distinguish whether an element is included in the max set or not.
    // We can obv check if the element is in one of the sets, but we can't check whether it's a subset of an existing element.
    // So, the burden of keeping this class consistent is on the caller.
    // It might be ok to do it anyway, because we might be able to prune the subsets later (and this check would prevent the same superset from being in both sets - see the todo comment below).

    public boolean addConfirmed(ColumnSet confirmed) {
        // return !candidates.contains(confirmed)
        //     && confirmeds.add(confirmed);
        return confirmeds.add(confirmed);
    }

    public boolean addCandidate(ColumnSet candidate) {
        // return !confirmeds.contains(candidate)
        //     && candidates.add(candidate);
        return candidates.add(candidate);
    }

    public boolean removeCandidate(ColumnSet candidate) {
        return candidates.remove(candidate);
    }

    public boolean moveCandidateToConfirmeds(ColumnSet candidate) {
        // return candidates.remove(candidate)
        //     && confirmeds.add(candidate);
        candidates.remove(candidate);
        return confirmeds.add(candidate);
    }

    // We can't keep the information about calling this method because the set might be mofidied later.
    public void pruneSubsets() {
        final Set<ColumnSet> supersets = new HashSet<ColumnSet>();

        // Process both confirmed and candidate elements.
        // Create a list from the stream to avoid concurrent modification issues.
        for (final ColumnSet set : elements().toList()) {
            boolean isSubset = false;
            List<ColumnSet> supersetsToDelete = new ArrayList<>();

            for (final ColumnSet superset : supersets) {
                if (set.isSupersetOf(superset)) {
                    // All supersets that are subsets of the current set needs to be purged.
                    supersetsToDelete.add(superset);
                }
                else if (superset.isSupersetOf(set)) {
                    isSubset = true;
                    // No need to check other supersets - if the set is a subset of one of them, it can't be a superset of another.
                    break;
                }
            }

            if (isSubset)
                continue; // The set is a subset of some superset so we don't need it anymore.

            // Remove all supersets that are subsets of the current set.
            supersets.removeAll(supersetsToDelete);
            supersets.add(set);
        }

        // Now we have all supersets, so we can filter the elements.
        confirmeds.removeIf(set -> !supersets.contains(set));
        candidates.removeIf(set -> !supersets.contains(set) || confirmeds.contains(set));
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("max(rhs=").append(forClass).append(", confirmeds: ");
        for (final ColumnSet set : confirmeds)
            sb.append(set).append(", ");
        if (confirmeds.size() == 0)
            sb.append(", ");
        sb.append("candidates: ");
        for (final ColumnSet set : candidates)
            sb.append(set).append(", ");
        if (candidates.size() > 0)
            sb.setLength(sb.length() - 2); // Remove last comma and space.

        sb.append(")");

        return sb.toString();
    }

    // #region Serialization

    public static class Serializer extends StdSerializer<MaxSet> {
        public Serializer() { this(null); }
        public Serializer(Class<MaxSet> t) { super(t); }

        @Override public void serialize(MaxSet set, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeNumberField("forClass", set.forClass);

            generator.writeFieldName("confirmeds");
            generator.getCodec().writeValue(generator, set.confirmeds);

            generator.writeFieldName("candidates");
            generator.getCodec().writeValue(generator, set.candidates);

            generator.writeBooleanField("isFinished", set.isFinished);

            generator.writeEndObject();
        }
    }

    public static class Deserializer extends StdDeserializer<MaxSet> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public MaxSet deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final var codec = parser.getCodec();
            final JsonNode node = codec.readTree(parser);

            final int forClass = node.get("forClass").asInt();

            final List<ColumnSet> confirmedsList = new ArrayList<>();
            for (final JsonNode item : node.get("confirmeds"))
                confirmedsList.add(codec.treeToValue(item, ColumnSet.class));

            final List<ColumnSet> candidatesList = new ArrayList<>();
            for (final JsonNode item : node.get("candidates"))
                candidatesList.add(codec.treeToValue(item, ColumnSet.class));

            final boolean isFinished = node.get("isFinished").asBoolean();

            return new MaxSet(forClass, confirmedsList, candidatesList, isFinished);
        }
    }

    // #endregion

}
