package ch.frostnova.cli.idx.sync;

import ch.frostnova.cli.idx.sync.console.ConsoleProgressBar;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Idx SYNC");

        //   consoleProgressExample();
        //   System.out.println();

        listDirectories();
    }

    private static void consoleProgressExample() throws Exception {

        int n = 150;
        try (ConsoleProgressBar progressBar = new ConsoleProgressBar("Testing progress")) {
            for (int i = 0; i <= n; i++) {
                progressBar.setProgress(i, n, "working…");
                Thread.sleep(53769 / n);
            }
        }
        System.out.println();
    }

    private static void listDirectories() {
        int maxRecurseDepth = Integer.MAX_VALUE;
        Set<Path> ignored = Stream.of("/dev", "/proc").map(Paths::get).collect(toSet());
        try (ConsoleProgressBar progressBar = new ConsoleProgressBar("Scanning \uD83D\uDE80")) {
            FileSystems.getDefault().getRootDirectories().forEach(root ->
                    traverse(root, path -> {
                        if (Files.isDirectory(path)) {
                            //  progressBar.println(path.toAbsolutePath());
                        }
                    }, path -> {
                        if (ignored.contains(path)) {
                            return false;
                        }
                        if (path.getNameCount() > maxRecurseDepth) {
                            return false;
                        }
                        try {
                            return !Files.isHidden(path);
                        } catch (IOException ex) {
                            return false;
                        }
                    }, (path, progress) -> progressBar.setProgress(progress, path.toAbsolutePath().toString())));
        }
    }

    private static void traverse(Path path, Consumer<Path> action, Predicate<Path> recurseCondition, BiConsumer<Path, Double> progressMonitor) {
        traverse(path, action, recurseCondition, progressMonitor, 0, 1);
    }

    private static void traverse(Path path, Consumer<Path> action, Predicate<Path> recurseCondition, BiConsumer<Path, Double> progressMonitor, double progressLowerBound, double progressUpperBound) {
        action.accept(path);
        progressMonitor.accept(path, progressLowerBound);
        if (Files.isDirectory(path) && recurseCondition.test(path)) {
            try {
                if (!Files.isSymbolicLink(path)) {
                    List<Path> list = Files.list(path).collect(Collectors.toList());
                    int n = list.size();
                    int index = 0;
                    for (Path child : list) {
                        double lowerBound = progressLowerBound + (progressUpperBound - progressLowerBound) * index / n;
                        double upperBound = progressLowerBound + (progressUpperBound - progressLowerBound) * (index + 1) / n;
                        index++;
                        traverse(child, action, recurseCondition, progressMonitor, lowerBound, upperBound);
                    }
                }
            } catch (IOException ex) {
                //System.err.println(ex.getMessage());
            }
        }
    }
}
