package de.uni.passau.server.clientdto;

import de.uni.passau.server.model.ClassNode;
import de.uni.passau.server.model.NegativeExampleNode;
import de.uni.passau.server.repository.ClassRepository.ClassNodeGroup;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public record Class(
    String id,
    String label,
    Double weight,
    int iteration,
    @Nullable NegativeExampleInfo example
) implements Serializable {

    public static Class fromNodes(ClassNode classNode, int iteration, @Nullable NegativeExampleNode exampleNode) {
        return new Class(
            classNode.getId(),
            classNode.getLabel(),
            classNode.getWeight(),
            iteration,
            exampleNode == null ? null : NegativeExampleInfo.fromNodes(exampleNode)
        );
    }

    public static Class fromNodes(ClassNodeGroup group) {
        return Class.fromNodes(group.classX(), group.iteration(), group.lastExample());
    }

}
