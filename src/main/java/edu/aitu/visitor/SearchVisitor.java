package edu.aitu.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class SearchVisitor extends TreeVisitor<List<String>> {
    private final Predicate<String> filenameCriterion;
    private final long minimumSize;
    private final List<String> matches = new ArrayList<>();

    public SearchVisitor(Predicate<String> filenameCriterion, long minimumSize) {
        this.filenameCriterion = Objects.requireNonNull(filenameCriterion, "filenameCriterion");
        if (minimumSize < 0) {
            throw new IllegalArgumentException("Minimum size must be non-negative");
        }
        this.minimumSize = minimumSize;
    }

    @Override
    protected void reset() {
        matches.clear();
    }

    @Override
    protected void onFile(FileNode file, VisitContext context) {
        if (file.size() >= minimumSize && filenameCriterion.test(file.name())) {
            matches.add(context.path());
        }
    }

    @Override
    protected List<String> result() {
        return matches.stream().sorted().toList();
    }
}
