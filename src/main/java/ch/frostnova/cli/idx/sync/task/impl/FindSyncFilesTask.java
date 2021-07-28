package ch.frostnova.cli.idx.sync.task.impl;

import ch.frostnova.cli.idx.sync.config.IdxSyncFile;
import ch.frostnova.cli.idx.sync.config.ObjectMappers;
import ch.frostnova.cli.idx.sync.task.Task;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static ch.frostnova.cli.idx.sync.io.FileSystemUtil.traverseAll;
import static java.util.stream.Collectors.toSet;

/**
 * Task which scans the file system for {@link IdxSyncFile}s.
 */
public class FindSyncFilesTask implements Task<Map<IdxSyncFile, Path>> {

    private final static Set<Path> ignored = Stream.of("/dev", "/proc").map(Paths::get).collect(toSet());
    private double progress;
    private String message;

    @Override
    public String getName() {
        return "Find sync files";
    }

    @Override
    public Map<IdxSyncFile, Path> run() {
        int maxRecurseDepth = 5;

        Map<IdxSyncFile, Path> result = new HashMap<>();

        traverseAll((path, progress) -> {
            this.progress = progress;
            this.message = path.toString();

            if (ignored.contains(path)) {
                return false;
            }
            if (path.getNameCount() > maxRecurseDepth) {
                return false;
            }

            if (Files.isRegularFile(path) && path.getFileName().equals(IdxSyncFile.FILENAME) && Files.isReadable(path)) {
                try {
                    URL url = path.toUri().toURL();
                    IdxSyncFile syncFile = ObjectMappers.yaml().readValue(url, IdxSyncFile.class);
                    result.put(syncFile, path);
                    return false;
                } catch (Exception ignored) {
                }
            }

            try {
                return !Files.isHidden(path);
            } catch (IOException ex) {
                return false;
            }
        });
        return result;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
