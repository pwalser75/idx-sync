package ch.frostnova.cli.idx.sync.io;

import ch.frostnova.cli.idx.sync.console.ConsoleTools;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class FileSystemUtil {

    private FileSystemUtil() {

    }

    public static void traverseAll(FileVisitor fileVisitor) {
        List<Path> list = StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false).collect(Collectors.toList());
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
        if (Files.isDirectory(path) && continueTraverse) {
            try {
                if (!Files.isSymbolicLink(path)) {
                    List<Path> list = Files.list(path).collect(Collectors.toList());
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
                }
            } catch (AccessDeniedException ignored) {
            } catch (IOException ex) {
                ConsoleTools.clearLine();
                System.err.println(ex.getClass().getSimpleName() + ":" + ex.getMessage());
            }
        }
        return true;
    }
}
