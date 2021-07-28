package ch.frostnova.cli.idx.sync.io;

import java.nio.file.Path;

public interface FileVisitor {

    /**
     * Visit a file. Return if traversing should continue on the same level (if file) or into children (if directory).
     *
     * @param path     current path
     * @param progress progress of the file visiting operation
     * @return continue traverse
     */
    boolean visit(Path path, double progress);
}