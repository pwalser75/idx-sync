package ch.frostnova.cli.idx.sync.io;

import java.nio.file.Path;

public interface FileVisitor {

    /**
     * Visit a file. For a directory, return if traversal should recurse into its content.
     *
     * @param path current path
     * @return recurse (if directory, ignored if file)
     */
    boolean visit(Path path);
}