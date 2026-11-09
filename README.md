# Assignment 06: Visitor Pattern

An in-memory file system implemented in Java 17. Three independent operations run on
the same tree: total size calculation, file search, and statistics. All demo and test data is synthetic.

Repository: https://github.com/bigstep832/visitor-pattern-assignment

## Running the Project

JDK 17 and Maven 3.9+ are required. Run the commands from the directory containing `pom.xml`.
Maven downloads dependencies from Maven Central during the first build. IntelliJ is not required.

```sh
mvn test
mvn package
java -jar target/visitor-pattern-1.0.0.jar
java -jar target/visitor-pattern-1.0.0.jar .png 1000
```

The application arguments are a filename suffix and a minimum size in bytes.
The defaults are `.txt` and `300`. Filename matching is case-sensitive.
The `SearchVisitor` API accepts any `Predicate<String>` applied to the filename, not the path.
The threshold is inclusive: `size >= minimumSize`. Negative thresholds are rejected.

On the development machine, Maven is bundled with IntelliJ but runs from the command line.
If `mvn` is not found, add the `bin` directory of your Maven installation to `PATH`.
The project was verified locally with Maven 3.9.11 and Microsoft OpenJDK 17.0.20.1.

## Custom Demo Tree

```text
campus/
  README.md                    120
  docs/
    architecture.txt           800
    notes.txt                  200
    archive/
      old-notes.txt            400
      empty/
  assets/
    logo.png                  2048
    empty.bin                    0
  scratch/
```

Output when run without arguments:

```text
Root: campus
Total size: 3568 bytes
Search: suffix=.txt, minimum=300
  docs/architecture.txt
  docs/archive/old-notes.txt
Matches: 2
Files: 6
Directories: 6
Maximum depth: 3
```

With `.png 1000`, the only match is `assets/logo.png`; the total size and statistics remain unchanged.

## Design and Double Dispatch

- `Element` declares `name()` and `accept(Visitor)`.
- `Node` stores the name and an internal parent reference; `FileNode` stores a nonnegative `long size`.
- `DirectoryNode.add(Node)` preserves tree invariants; `children()` returns an immutable snapshot.
- `Visitor` declares both `visit(FileNode)` and `visit(DirectoryNode)` from the start.
- `TreeVisitor<R>` provides a shared lifecycle and traversal for all operations.
- `TotalSizeVisitor`, `SearchVisitor`, and `StatisticsVisitor` contain only the logic for their respective results.

In `element.accept(visitor)`, the element's runtime type determines which `accept` method runs.
Inside `FileNode.accept`, `this` has the compile-time type `FileNode`, so the compiler
selects the `visit(FileNode)` overload. The call is made on the visitor object;
in this implementation, the shared `TreeVisitor` handler calls the overridden `onFile` method.
For a directory, `visit(DirectoryNode)` is selected in the same way. Type checks,
reflection, and casts are not used to select behavior.

## Traversal Ownership and State

Visitors own the traversal through the shared `TreeVisitor`; elements only dispatch calls.
`evaluate(root)` resets the result, pushes the root onto an explicit stack, and processes
each reachable node once. The directory handler pushes its children onto the stack
in reverse order, preserving insertion order during depth-first traversal. No recursion is used.

All operations start through `visitor.evaluate(root)`. Calling `accept` directly with a
custom `Visitor` is allowed. A direct call with a `TreeVisitor` outside `evaluate`
is rejected because no path or depth context has been established.

Visitor instances can be reused **sequentially**. Traversal state is cleared in
`finally`, even if a user-supplied predicate throws an exception; the next evaluation
calls `reset` again. Search and statistics results are immutable.
A nested `evaluate` call on the same visitor is rejected. Thread safety is not guaranteed,
and the tree must not be modified during traversal. Predicates must have no side effects:
they must not modify the tree or invoke visitor dispatch or evaluation through `accept`, `visit`, or `evaluate`.

The selected root's path is `.`; the root directory's name is excluded from descendant paths.
If a single file is selected as the root, its relative path is also `.`.
Selecting a subdirectory starts a new relative path and depth context. The separator is always `/`.
Search results use Java's natural lexicographic ordering (`String.compareTo`).
The root has depth 0; each child's depth is its parent's depth + 1. Empty directories are counted too.

## Invariants and Limitations

The public API rejects negative sizes, duplicate sibling names (including a file
and a directory with the same name), a second parent, and adding a directory to itself
or adding one of its ancestors as a child. All conditions are checked before the tree is changed.
A name cannot be empty, whitespace-only, `.`, or `..`, and cannot contain `/`, `\`, or control characters.
Names are case-sensitive; identical names in different directories are allowed.
Names and sizes are immutable. Removal and move operations are outside this API's scope.

Invalid values and mutations throw `IllegalArgumentException`; `null` arguments throw
`NullPointerException`; invalid lifecycle usage throws `IllegalStateException`.
Overflow when summing `long` values throws `ArithmeticException` instead of producing a negative total.
Depth is limited by the `int` range and available memory; depth overflow is also checked.
The sealed hierarchy fixes the permitted node types and prevents external implementations
of `Element` from bypassing the invariants.

## Extending the Design

The third visitor was added in a separate commit, `feat: add statistics visitor without changing element contracts`.
Only `Statistics`, `StatisticsVisitor`, and its tests were added. `FileNode`,
`DirectoryNode`, `Visitor`, `accept`, and the shared traversal remained unchanged.
A new operation can be added by extending `TreeVisitor<R>`.

Adding a new **element type** requires more changes: update the sealed `permits` declarations,
add an overload to `Visitor`, and define traversal and operation behavior for the new type.
This is the Visitor trade-off: adding operations is convenient when element types remain stable.
Traversal performs O(n) visits; search also sorts k matches in O(k log k) time.
The cost of constructing path strings depends on their lengths; memory is used for the stack and results.

## Verification

Verified on Java 17: **25 tests, 0 failures, 0 errors, 0 skipped**.
`mvn package` produces an executable JAR; both CLI configurations shown above were run successfully.

| Class | Tests | Requirements |
| --- | ---: | --- |
| `DispatchTest` | 1 | T1: correct overloads, no traversal inside elements |
| `TotalSizeVisitorTest` | 4 | T2, T6, T7: total size, repeated evaluation, empty root, overflow |
| `SearchVisitorTest` | 7 | T3, T4, T6, T7: filters, paths, sorting, reset, and exceptions |
| `StatisticsVisitorTest` | 5 | T5, T6, T7: counts, depth, a new root, 2000 levels |
| `TreeIntegrityTest` | 6 | T8: invalid mutations, snapshots, names, and sizes |
| `DemoTest` | 2 | Demo output and configuration changes |

Expected test values are specified manually, not calculated using the visitors under test.
The test trees differ from the demo tree.

## Materials

- [PDF report](docs/report.pdf): 3 pages in English, including a landscape page with the UML diagram.

## Sources and Assistance

The requirements come from the supplied `Assignment_6_Visitor_Pattern.pdf` (AITU, Assignment 06).
References: [Java SE 17, JLS 15.12](https://docs.oracle.com/javase/specs/jls/se17/html/jls-15.html#jls-15.12)
and the [JUnit 5.10.2 User Guide](https://docs.junit.org/5.10.2/user-guide/).
The code, tests, and report were prepared with ChatGPT/Codex assistance. This is not a claim
of independent student authorship. Before submission, follow the course's rules on permitted
AI assistance and be prepared to explain and modify the implementation independently.
