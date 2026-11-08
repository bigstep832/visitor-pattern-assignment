package edu.aitu.visitor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DirectoryNode extends Node {
    private final Map<String, Node> children = new LinkedHashMap<>();

    public DirectoryNode(String name) {
        super(name);
    }

    public DirectoryNode add(Node child) {
        Objects.requireNonNull(child, "child");
        if (children.containsKey(child.name())) {
            throw new IllegalArgumentException("Duplicate sibling name: " + child.name());
        }
        for (Node ancestor = this; ancestor != null; ancestor = ancestor.parentNode()) {
            if (ancestor == child) {
                throw new IllegalArgumentException("Adding this node would create a cycle");
            }
        }
        child.attachTo(this);
        children.put(child.name(), child);
        return this;
    }

    public List<Node> children() {
        return List.copyOf(children.values());
    }

    @Override
    public void accept(Visitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visit(this);
    }
}
