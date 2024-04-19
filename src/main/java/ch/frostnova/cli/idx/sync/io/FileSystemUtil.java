package ch.frostnova.cli.idx.sync.io;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;
import java.util.stream.Stream;
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

    public static void traverseAll(FileVisitor fileVisitor, Consumer<Double> progressConsumer) {
        traverse(StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false), fileVisitor, progressConsumer);
    }

    public static void traverse(Path path, FileVisitor fileVisitor, Consumer<Double> progressConsumer) {
        traverse(Stream.of(path), fileVisitor, progressConsumer);
    }

    public static void traverse(Stream<Path> pathStream, FileVisitor fileVisitor, Consumer<Double> progressConsumer) {
        traverse(pathStream.collect(toList()), fileVisitor, progressConsumer);
    }

    private static void traverse(List<Path> paths, FileVisitor fileVisitor, Consumer<Double> progressConsumer) {
        if (paths.isEmpty()) {
            return;
        }
        var weight = 1d / paths.size();

        var progressCollector = new ProgressCollector();
        Consumer<Double> doneConsumer = progress -> {
            progressCollector.done(progress);
            progressConsumer.accept(progressCollector.getTotalProgress());
        };

        ExecutorService executorService = ForkJoinPool.commonPool();
        var phaser = new Phaser(1);
        paths.forEach(path -> {
            phaser.register();
            executorService.submit(() -> {
                process(executorService, phaser, path, fileVisitor, weight, doneConsumer);
            });
        });
        phaser.arriveAndAwaitAdvance();
        progressConsumer.accept(1d);
    }

    public static void traverseAll(ProgressFileVisitor fileVisitor) {
        var list = StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false).collect(toList());
        var n = list.size();
        var index = 0;
        for (var child : list) {
            var lowerBound = (double) index / n;
            var upperBound = (double) (index + 1) / n;
            index++;
            traverse(child, fileVisitor, lowerBound, upperBound);
        }
    }

    public static void traverse(Path path, ProgressFileVisitor fileVisitor) {
        traverse(path, fileVisitor, 0, 1);
    }

    public static boolean traverse(Path path, ProgressFileVisitor fileVisitor, double progressLowerBound, double progressUpperBound) {
        var continueTraverse = fileVisitor.visit(path, progressLowerBound);
        if (continueTraverse && isDirectory(path) && isReadable(path) && !isSymbolicLink(path)) {
            try {
                var list = list(path).collect(toList());
                var n = list.size();
                var index = 0;
                for (var child : list) {
                    var lowerBound = progressLowerBound + (progressUpperBound - progressLowerBound) * index / n;
                    var upperBound = progressLowerBound + (progressUpperBound - progressLowerBound) * (index + 1) / n;
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

    private static void process(ExecutorService executorService, Phaser phaser, Path path, FileVisitor fileVisitor,
                                double weight, Consumer<Double> doneConsumer) {
        try {
            var recurse = fileVisitor.visit(path);
            if (Files.isDirectory(path) && recurse) {
                try {
                    var paths = list(path).collect(toList());
                    if (!paths.isEmpty()) {
                        var childWeight = weight / paths.size();
                        paths.forEach(child -> {
                            phaser.register();
                            executorService.submit(() -> {
                                process(executorService, phaser, child, fileVisitor, childWeight, doneConsumer);
                            });
                        });
                        return;
                    }
                } catch (AccessDeniedException ignored) {
                    //
                } catch (IOException ex) {
                    System.err.println(ex.getClass().getSimpleName() + ":" + ex.getMessage());
                }
            }
            doneConsumer.accept(weight);
        } finally {
            phaser.arriveAndDeregister();
        }
    }

    private static class ProgressCollector {

        private volatile double totalProgress;

        void done(double progress) {
            totalProgress += progress;
        }

        public double getTotalProgress() {
            return totalProgress;
        }
    }
}