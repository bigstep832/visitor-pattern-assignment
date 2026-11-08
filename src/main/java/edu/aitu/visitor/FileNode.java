package edu.aitu.visitor;

import java.util.Objects;

public final class FileNode extends Node {
    private final long size;

    public FileNode(String name, long size) {
        super(name);
        if (size < 0) {
            throw new IllegalArgumentException("File size must be non-negative");
        }
        this.size = size;
    }

    public long size() {
        return size;
    }

    @Override
    public void accept(Visitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visit(this);
    }
}
