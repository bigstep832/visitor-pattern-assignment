package edu.aitu.visitor;

public record VisitContext(String path, int depth) {
    VisitContext child(String name) {
        String childPath = depth == 0 ? name : path + "/" + name;
        return new VisitContext(childPath, Math.incrementExact(depth));
    }
}
