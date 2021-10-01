package ch.frostnova.cli.idx.sync;

import ch.frostnova.cli.idx.sync.console.ConsoleProgressBar;
import ch.frostnova.cli.idx.sync.console.ProgressBarStyle;
import ch.frostnova.cli.idx.sync.filter.PathFilter;
import ch.frostnova.cli.idx.sync.io.FileSystemUtil;
import ch.frostnova.cli.idx.sync.util.Invocation;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class ScanAll {

    public static void main(String[] args) throws IOException {

        Predicate<Path> filter = PathFilter.NONE;

        ConsoleProgressBar consoleProgressBar = new ConsoleProgressBar(ProgressBarStyle.autodetect());

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("files.txt"), StandardCharsets.UTF_8))) {
            FileSystemUtil.traverseAll((path, progress) -> {
                consoleProgressBar.printProgress(progress, "Scan", path.toString());
                if (filter.test(path)) {
                    return !Files.isDirectory(path);
                }
                Invocation.runUnchecked(() -> writer.write(path + "\n"));
                return true;
            });
        }
        consoleProgressBar.printDone("Scan", "done");
    }
}
