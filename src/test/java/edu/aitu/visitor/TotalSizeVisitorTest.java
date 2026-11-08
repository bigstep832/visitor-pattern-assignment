package edu.aitu.visitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TotalSizeVisitorTest {
    @Test
    void sumsNestedFilesExactlyOnceAndIgnoresDirectoryNames() {
        DirectoryNode root = new DirectoryNode("root")
                .add(new FileNode("zero", 0))
                .add(new FileNode("a", 12))
                .add(new DirectoryNode("nested").add(new FileNode("b", 23))
                        .add(new DirectoryNode("deep").add(new FileNode("c", 45))))
                .add(new DirectoryNode("empty"));
        assertEquals(80L, new TotalSizeVisitor().evaluate(root));
    }

    @Test
    void sequentialRunsStartFreshForTheSameAndDifferentRoots() {
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        FileNode file = new FileNode("only", 19);
        assertEquals(19L, visitor.evaluate(file));
        assertEquals(19L, visitor.evaluate(file));
        assertEquals(0L, visitor.evaluate(new DirectoryNode("empty")));
    }

    @Test
    void overflowIsExplicitAndTheVisitorRecoversForItsNextRun() {
        DirectoryNode tooLarge = new DirectoryNode("large")
                .add(new FileNode("a", Long.MAX_VALUE)).add(new FileNode("b", 1));
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        assertThrows(ArithmeticException.class, () -> visitor.evaluate(tooLarge));
        assertEquals(2L, visitor.evaluate(new FileNode("small", 2)));
    }

    @Test
    void operationRejectsDirectVisitWithoutLifecycleAndNullRoot() {
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        assertThrows(IllegalStateException.class, () -> new FileNode("a", 1).accept(visitor));
        assertThrows(NullPointerException.class, () -> visitor.evaluate(null));
        assertEquals(1L, visitor.evaluate(new FileNode("a", 1)));
    }
}
