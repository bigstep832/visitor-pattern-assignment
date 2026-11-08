package edu.aitu.visitor;

public final class TotalSizeVisitor extends TreeVisitor<Long> {
    private long total;

    @Override
    protected void reset() {
        total = 0;
    }

    @Override
    protected void onFile(FileNode file, VisitContext context) {
        total = Math.addExact(total, file.size());
    }

    @Override
    protected Long result() {
        return total;
    }
}
