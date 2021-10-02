package ch.frostnova.cli.idx.sync;

import ch.frostnova.cli.idx.sync.console.ConsoleProgressBar;
import ch.frostnova.cli.idx.sync.filter.PathFilter;
import ch.frostnova.cli.idx.sync.io.FileSystemUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ScanAll {

    public static void main(String[] args) throws IOException {

        Predicate<Path> filter = PathFilter.NONE;

        long time = System.nanoTime();
        AtomicLong count = new AtomicLong();

        // Done in 0.95 sec, visited 92723 file
        // Done in 0.94 sec, visited 92723 files
        /*
        FileSystemUtil.traverse(Path.of("W:/"), (path, progress) -> {
            count.incrementAndGet();
            return true;
        });
*/
        // Done in 0.33 sec, visited 92723 files
        // Done in 0.34 sec, visited 92723 files


        PrintProgress printProgress = new PrintProgress();
        FileSystemUtil.traverse(Path.of("X:/"), path -> {
            count.incrementAndGet();
            return true;
        }, printProgress);

        time = System.nanoTime() - time;
        System.out.printf("Done in %.2f sec, visited %d files", time * 1e-9, count.get());
    }

    private static class PrintProgress implements Consumer<Double> {

        private final ConsoleProgressBar progressBar = new ConsoleProgressBar();
        private double lastReported;

        @Override
        public void accept(Double progress) {
            if (progress - lastReported > 0.001) {
                progressBar.printProgress(progress, "Scan", "abc");
                lastReported = progress;
            }
        }
    }
}
