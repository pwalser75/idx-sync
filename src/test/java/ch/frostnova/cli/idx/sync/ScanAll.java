package ch.frostnova.cli.idx.sync;

import ch.frostnova.cli.idx.sync.filter.PathFilter;
import ch.frostnova.cli.idx.sync.io.FileSystemUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
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

        FileSystemUtil.traverseAll(path -> {
            count.incrementAndGet();
            return true;
        });

        time = System.nanoTime() - time;
        System.out.printf("Done in %.2f sec, visited %d files", time * 1e-9, count.get());
    }
}
