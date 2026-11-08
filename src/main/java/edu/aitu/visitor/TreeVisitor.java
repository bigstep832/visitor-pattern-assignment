package edu.aitu.visitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/** Owns traversal and the lifecycle of a sequentially reusable operation. */
public abstract class TreeVisitor<R> implements Visitor {
    private final Deque<Frame> pending = new ArrayDeque<>();
    private VisitContext current;
    private boolean running;

    public final R evaluate(Element root) {
        Objects.requireNonNull(root, "root");
        if (running) {
            throw new IllegalStateException("Visitor cannot be run reentrantly");
        }
        running = true;
        try {
            reset();
            pending.push(new Frame(root, new VisitContext(".", 0)));
            while (!pending.isEmpty()) {
                Frame frame = pending.pop();
                current = frame.context();
                frame.element().accept(this);
            }
            return result();
        } finally {
            pending.clear();
            current = null;
            running = false;
        }
    }

    @Override
    public final void visit(FileNode file) {
        requireEvaluation();
        onFile(file, current);
    }

    @Override
    public final void visit(DirectoryNode directory) {
        requireEvaluation();
        onDirectory(directory, current);
        List<Node> children = directory.children();
        for (int index = children.size() - 1; index >= 0; index--) {
            Node child = children.get(index);
            pending.push(new Frame(child, current.child(child.name())));
        }
    }

    private void requireEvaluation() {
        if (!running || current == null) {
            throw new IllegalStateException("Start an operation with evaluate(root)");
        }
    }

    protected abstract void reset();

    protected abstract void onFile(FileNode file, VisitContext context);

    protected void onDirectory(DirectoryNode directory, VisitContext context) {
        // Operations may ignore directory data; traversal still visits its children.
    }

    protected abstract R result();

    private record Frame(Element element, VisitContext context) { }
}
