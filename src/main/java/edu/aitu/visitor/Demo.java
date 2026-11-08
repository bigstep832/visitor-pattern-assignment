package edu.aitu.visitor;

import java.util.List;

public final class Demo {
    private Demo() { }

    public static void main(String[] args) {
        if (args.length > 2) {
            throw new IllegalArgumentException("Usage: java -jar visitor-pattern-1.0.0.jar [suffix] [minimumBytes]");
        }
        String suffix = args.length > 0 ? args[0] : ".txt";
        long minimumSize = args.length > 1 ? Long.parseLong(args[1]) : 300;
        System.out.print(run(suffix, minimumSize));
    }

    public static String run(String suffix, long minimumSize) {
        DirectoryNode root = sampleTree();
        long total = new TotalSizeVisitor().evaluate(root);
        List<String> matches = new SearchVisitor(name -> name.endsWith(suffix), minimumSize).evaluate(root);
        Statistics statistics = new StatisticsVisitor().evaluate(root);
        StringBuilder output = new StringBuilder();
        output.append("Root: ").append(root.name()).append('\n');
        output.append("Total size: ").append(total).append(" bytes\n");
        output.append("Search: suffix=").append(suffix).append(", minimum=").append(minimumSize).append('\n');
        for (String path : matches) {
            output.append("  ").append(path).append('\n');
        }
        output.append("Matches: ").append(matches.size()).append('\n');
        output.append("Files: ").append(statistics.fileCount()).append('\n');
        output.append("Directories: ").append(statistics.directoryCount()).append('\n');
        output.append("Maximum depth: ").append(statistics.maximumDepth()).append('\n');
        return output.toString();
    }

    private static DirectoryNode sampleTree() {
        DirectoryNode archive = new DirectoryNode("archive")
                .add(new FileNode("old-notes.txt", 400)).add(new DirectoryNode("empty"));
        DirectoryNode docs = new DirectoryNode("docs")
                .add(new FileNode("architecture.txt", 800))
                .add(new FileNode("notes.txt", 200)).add(archive);
        DirectoryNode assets = new DirectoryNode("assets")
                .add(new FileNode("logo.png", 2048)).add(new FileNode("empty.bin", 0));
        return new DirectoryNode("campus").add(new FileNode("README.md", 120))
                .add(docs).add(assets).add(new DirectoryNode("scratch"));
    }
}
