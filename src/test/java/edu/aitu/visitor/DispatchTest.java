package edu.aitu.visitor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DispatchTest {
    @Test
    void eachElementSelectsItsOwnOverloadWithoutWalkingChildren() {
        List<String> calls = new ArrayList<>();
        Visitor visitor = new Visitor() {
            public void visit(FileNode file) { calls.add("file:" + file.name()); }
            public void visit(DirectoryNode directory) { calls.add("directory:" + directory.name()); }
        };
        Element file = new FileNode("note.txt", 7);
        Element directory = new DirectoryNode("folder").add(new FileNode("hidden.txt", 99));
        file.accept(visitor);
        directory.accept(visitor);
        assertEquals(List.of("file:note.txt", "directory:folder"), calls);
    }
}
