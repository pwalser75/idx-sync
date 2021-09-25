package ch.frostnova.cli.idx.sync.io;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

import static ch.frostnova.cli.idx.sync.console.Console.clearLine;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.isSymbolicLink;
import static java.nio.file.Files.list;
import static java.util.stream.Collectors.toList;

public final class FileSystemUtil {

    private FileSystemUtil() {

    }

    public static void traverseAll(FileVisitor fileVisitor) {
        List<Path> list = StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false).collect(toList());
        int n = list.size();
        int index = 0;
        for (Path child : list) {
            double lowerBound = (double) index / n;
            double upperBound = (double) (index + 1) / n;
            index++;
            traverse(child, fileVisitor, lowerBound, upperBound);
        }
    }

    public static void traverse(Path path, FileVisitor fileVisitor) {
        traverse(path, fileVisitor, 0, 1);
    }

    public static boolean traverse(Path path, FileVisitor fileVisitor, double progressLowerBound, double progressUpperBound) {
        boolean continueTraverse = fileVisitor.visit(path, progressLowerBound);
        if (continueTraverse && isDirectory(path) && isReadable(path) && !isSymbolicLink(path)) {
            try {
                List<Path> list = list(path).collect(toList());
                int n = list.size();
                int index = 0;
                for (Path child : list) {
                    double lowerBound = progressLowerBound + (progressUpperBound - progressLowerBound) * index / n;
                    double upperBound = progressLowerBound + (progressUpperBound - progressLowerBound) * (index + 1) / n;
                    index++;
                    if (!traverse(child, fileVisitor, lowerBound, upperBound)) {
                        return true;
                    }
                }
            } catch (AccessDeniedException ignored) {
            } catch (IOException ex) {
                clearLine();
                System.err.println(ex.getClass().getSimpleName() + ":" + ex.getMessage());
            }
        }
        return true;
    }
}
