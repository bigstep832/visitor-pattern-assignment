package edu.aitu.visitor;

public sealed interface Element permits Node {
    String name();

    void accept(Visitor visitor);
}
