package edu.aitu.visitor;

import java.util.Objects;

public abstract sealed class Node implements Element permits FileNode, DirectoryNode {
    private final String name;
    private DirectoryNode parent;

    protected Node(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank() || name.equals(".") || name.equals("..")
                || name.contains("/") || name.contains("\\")
                || name.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Name must be a non-empty path segment");
        }
        this.name = name;
    }

    @Override
    public final String name() {
        return name;
    }

    final DirectoryNode parentNode() {
        return parent;
    }

    final void attachTo(DirectoryNode directory) {
        if (parent != null) {
            throw new IllegalArgumentException("Node already belongs to a directory");
        }
        parent = Objects.requireNonNull(directory, "directory");
    }
}
