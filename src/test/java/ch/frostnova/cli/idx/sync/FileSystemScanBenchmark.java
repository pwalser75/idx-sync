package ch.frostnova.cli.idx.sync;

import ch.frostnova.cli.idx.sync.console.Console;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class FileSystemScanBenchmark {

    private final static Set<Path> IGNORED = Stream.of("/dev", "/proc").map(Paths::get).collect(toSet());

    public static void main(String[] args) {

        benchmark((path, count) -> traverseDirIo(path, count));
        benchmark((path, count) -> traverseDirNio(path, count));
    }

    private static void benchmark(Strategy strategy) {
        AtomicInteger fileCount = new AtomicInteger();
        List<Path> roots = StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false).collect(toList());
        double timeSec = benchmark(() -> {
            for (Path root : roots) {
                strategy.visitUnchecked(root, fileCount);
            }
        });
        Console.printf("\nFound %,d files in %.2f sec\n", fileCount.get(), timeSec);
    }

    private static double benchmark(Task task) {
        long timeNs = System.nanoTime();
        task.runUnchecked();
        timeNs = System.nanoTime() - timeNs;
        return timeNs * 1e-9;
    }

    @FunctionalInterface
    public interface Strategy {
        void visit(Path root, AtomicInteger count) throws Throwable;

        default void visitUnchecked(Path root, AtomicInteger count) {
            try {
                visit(root, count);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    @FunctionalInterface
    public interface Task {
        void run() throws Throwable;

        default void runUnchecked() {
            try {
                run();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    // Found 3’266’291 files in 17.21 sec
    // Found 3’266’291 files in 8.64 sec
    private static void traverseDirNio(Path dir, AtomicInteger fileCount) throws IOException {
        if (IGNORED.contains(dir)) {
            return;
        }

        Files.list(dir).forEach(path -> {
            fileCount.incrementAndGet();
            if (Files.isDirectory(path) && Files.isReadable(path) && !Files.isSymbolicLink(path)) {
                try {
                    traverseDirNio(path, fileCount);
                } catch (IOException e) {
                    System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        });
    }

    private static void traverseDirIo(Path path, AtomicInteger fileCount) throws IOException {
        traverseDirIo(path.toFile(), fileCount);
    }

    // Found 6’532’239 files in 21.62 sec
    private static void traverseDirIo(File dir, AtomicInteger fileCount) throws IOException {
        if (IGNORED.contains(dir.toPath())) {
            return;
        }
        for (String filename : dir.list()) {
            File file = new File(dir, filename);
            fileCount.incrementAndGet();
            if (file.isDirectory() && !Files.isSymbolicLink(file.toPath()) && file.canRead()) {
                try {
                    traverseDirIo(file, fileCount);
                } catch (IOException e) {
                    System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }
}
