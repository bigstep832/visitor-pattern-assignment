package edu.aitu.visitor;

public final class StatisticsVisitor extends TreeVisitor<Statistics> {
    private long fileCount;
    private long directoryCount;
    private int maximumDepth;

    @Override
    protected void reset() {
        fileCount = 0;
        directoryCount = 0;
        maximumDepth = 0;
    }

    @Override
    protected void onFile(FileNode file, VisitContext context) {
        fileCount = Math.incrementExact(fileCount);
        includeDepth(context);
    }

    @Override
    protected void onDirectory(DirectoryNode directory, VisitContext context) {
        directoryCount = Math.incrementExact(directoryCount);
        includeDepth(context);
    }

    private void includeDepth(VisitContext context) {
        maximumDepth = Math.max(maximumDepth, context.depth());
    }

    @Override
    protected Statistics result() {
        return new Statistics(fileCount, directoryCount, maximumDepth);
    }
}
