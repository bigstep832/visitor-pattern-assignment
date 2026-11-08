package edu.aitu.visitor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreeIntegrityTest {
    @Test
    void negativeFileSizeIsRejectedButZeroIsValid() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("bad", -1));
        assertEquals(0, new FileNode("empty", 0).size());
    }

    @Test
    void duplicateFileAndDirectoryNamesAreRejectedWithoutAttachingTheCandidate() {
        DirectoryNode root = new DirectoryNode("root").add(new FileNode("same", 1));
        DirectoryNode rejected = new DirectoryNode("same");
        assertThrows(IllegalArgumentException.class, () -> root.add(rejected));
        assertThrows(IllegalArgumentException.class, () -> root.add(new FileNode("same", 2)));
        DirectoryNode other = new DirectoryNode("other").add(rejected);
        assertThrows(IllegalArgumentException.class, () -> other.add(new DirectoryNode("same")));
        assertThrows(IllegalArgumentException.class, () -> other.add(new FileNode("same", 3)));
        assertEquals(1, root.children().size());
        assertEquals(List.of(rejected), other.children());
    }

    @Test
    void aNodeCannotBelongToTwoParents() {
        FileNode file = new FileNode("shared.txt", 1);
        DirectoryNode first = new DirectoryNode("first").add(file);
        DirectoryNode second = new DirectoryNode("second");
        assertThrows(IllegalArgumentException.class, () -> second.add(file));
        assertEquals(List.of(file), first.children());
        assertTrue(second.children().isEmpty());
        DirectoryNode folder = new DirectoryNode("folder");
        first.add(folder);
        assertThrows(IllegalArgumentException.class, () -> second.add(folder));
        assertTrue(second.children().isEmpty());
    }

    @Test
    void selfAndAncestorCyclesAreRejectedWhileNestingRemainsValid() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode nested = new DirectoryNode("nested");
        DirectoryNode leaf = new DirectoryNode("leaf");
        root.add(nested);
        nested.add(leaf);
        assertThrows(IllegalArgumentException.class, () -> root.add(root));
        assertThrows(IllegalArgumentException.class, () -> leaf.add(root));
        assertTrue(leaf.children().isEmpty());
        assertEquals(List.of(nested), root.children());
        leaf.add(new FileNode("ok", 5));
        assertEquals(1, leaf.children().size());
    }

    @Test
    void childrenAreImmutableSnapshotsAndNamesCanRepeatInDifferentDirectories() {
        DirectoryNode root = new DirectoryNode("root");
        List<Node> before = root.children();
        root.add(new FileNode("note", 2));
        assertTrue(before.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> root.children().clear());
        DirectoryNode nested = new DirectoryNode("nested").add(new FileNode("note", 3));
        root.add(nested);
        assertEquals(2, root.children().size());
    }

    @Test
    void ambiguousNamesAndNullArgumentsAreRejected() {
        for (String name : List.of("", " ", ".", "..", "a/b", "a\\b", "a\nb")) {
            assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(name));
        }
        assertThrows(NullPointerException.class, () -> new FileNode(null, 1));
        assertThrows(NullPointerException.class, () -> new DirectoryNode("root").add(null));
    }
}
