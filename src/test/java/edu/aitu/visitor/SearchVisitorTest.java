package edu.aitu.visitor;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class SearchVisitorTest {
    @Test
    void combinesFilenameAndInclusiveSizeConstraints() {
        DirectoryNode root = new DirectoryNode("root")
                .add(new FileNode("small.txt", 9)).add(new FileNode("exact.txt", 10))
                .add(new FileNode("large.bin", 50)).add(new FileNode("large.txt", 50));
        assertEquals(List.of("exact.txt", "large.txt"),
                new SearchVisitor(name -> name.endsWith(".txt"), 10).evaluate(root));
        assertEquals(List.of("large.bin"),
                new SearchVisitor(name -> name.equals("large.bin"), 0).evaluate(root));
    }

    @Test
    void pathsAreSortedAndRelativeToTheSelectedSubtree() {
        DirectoryNode selected = new DirectoryNode("selected")
                .add(new FileNode("z.txt", 1))
                .add(new DirectoryNode("beta").add(new FileNode("same.txt", 1)))
                .add(new DirectoryNode("alpha").add(new FileNode("same.txt", 1)))
                .add(new FileNode("a.txt", 1));
        new DirectoryNode("outside").add(selected);
        assertEquals(List.of("a.txt", "alpha/same.txt", "beta/same.txt", "z.txt"),
                new SearchVisitor(name -> true, 0).evaluate(selected));
    }

    @Test
    void repeatedSearchReturnsIndependentImmutableResults() {
        SearchVisitor visitor = new SearchVisitor(name -> true, 0);
        DirectoryNode root = new DirectoryNode("root").add(new FileNode("note", 1));
        List<String> first = visitor.evaluate(root);
        assertEquals(first, visitor.evaluate(root));
        assertEquals(List.of(), visitor.evaluate(new DirectoryNode("empty")));
        assertEquals(List.of("note"), first);
        assertThrows(UnsupportedOperationException.class, () -> first.add("other"));
    }

    @Test
    void emptyNoMatchAndFileRootCasesHaveExplicitResults() {
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 0);
        assertEquals(List.of(), visitor.evaluate(new DirectoryNode("empty")));
        assertEquals(List.of(), visitor.evaluate(new FileNode("data.bin", 100)));
        assertEquals(List.of("."), visitor.evaluate(new FileNode("note.txt", 0)));
    }

    @Test
    void predicateFailureDoesNotLeavePendingNodesInTheNextRun() {
        AtomicBoolean fail = new AtomicBoolean(true);
        SearchVisitor visitor = new SearchVisitor(name -> {
            if (fail.get() && name.equals("bad")) {
                throw new IllegalArgumentException("Predicate failure");
            }
            return true;
        }, 0);
        DirectoryNode root = new DirectoryNode("root").add(new FileNode("good", 1))
                .add(new FileNode("bad", 1)).add(new FileNode("pending", 1));
        assertThrows(IllegalArgumentException.class, () -> visitor.evaluate(root));
        fail.set(false);
        assertEquals(List.of("."), visitor.evaluate(new FileNode("new", 1)));
    }

    @Test
    void invalidConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(name -> true, -1));
        assertThrows(NullPointerException.class, () -> new SearchVisitor(null, 0));
    }

    @Test
    void reentrantEvaluationIsRejectedAndDoesNotCorruptTheOuterRun() {
        SearchVisitor[] visitor = new SearchVisitor[1];
        FileNode root = new FileNode("note", 1);
        visitor[0] = new SearchVisitor(name -> {
            assertThrows(IllegalStateException.class, () -> visitor[0].evaluate(root));
            return true;
        }, 0);
        assertEquals(List.of("."), visitor[0].evaluate(root));
    }
}
