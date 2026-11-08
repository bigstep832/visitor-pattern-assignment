package edu.aitu.visitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsVisitorTest {
    @Test
    void countsRootAndEveryNodeIncludingTheDeepestEmptyDirectory() {
        DirectoryNode root = new DirectoryNode("root").add(new FileNode("a", 1))
                .add(new DirectoryNode("nested").add(new FileNode("b", 2))
                        .add(new DirectoryNode("deep").add(new DirectoryNode("empty"))));
        assertEquals(new Statistics(2, 4, 3), new StatisticsVisitor().evaluate(root));
    }

    @Test
    void emptyDirectoryCountsAsOneDirectoryAtDepthZero() {
        assertEquals(new Statistics(0, 1, 0),
                new StatisticsVisitor().evaluate(new DirectoryNode("empty")));
    }

    @Test
    void fileRootHasNoDirectoriesAndDepthZero() {
        assertEquals(new Statistics(1, 0, 0),
                new StatisticsVisitor().evaluate(new FileNode("only", 9)));
    }

    @Test
    void repeatedRunsAndSubtreeRootsResetAllStatistics() {
        StatisticsVisitor visitor = new StatisticsVisitor();
        DirectoryNode nested = new DirectoryNode("nested").add(new FileNode("a", 1));
        DirectoryNode root = new DirectoryNode("root").add(nested);
        Statistics first = visitor.evaluate(root);
        assertEquals(new Statistics(1, 2, 2), first);
        assertEquals(first, visitor.evaluate(root));
        assertEquals(new Statistics(1, 1, 1), visitor.evaluate(nested));
        assertEquals(new Statistics(0, 1, 0), visitor.evaluate(new DirectoryNode("empty")));
        assertEquals(new Statistics(1, 2, 2), first);
    }

    @Test
    void iterativeTraversalSupportsTwoThousandLevelsWithoutRecursiveCalls() {
        DirectoryNode root = new DirectoryNode("bottom");
        for (int index = 0; index < 2000; index++) {
            root = new DirectoryNode("level" + index).add(root);
        }
        assertEquals(new Statistics(0, 2001, 2000), new StatisticsVisitor().evaluate(root));
    }
}
