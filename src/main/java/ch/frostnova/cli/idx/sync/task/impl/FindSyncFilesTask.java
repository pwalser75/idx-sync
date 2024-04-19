package ch.frostnova.cli.idx.sync.task.impl;

import ch.frostnova.cli.idx.sync.config.IdxSyncFile;
import ch.frostnova.cli.idx.sync.filter.PathFilter;
import ch.frostnova.cli.idx.sync.task.Task;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static ch.frostnova.cli.idx.sync.config.IdxSyncFile.FILENAME;
import static ch.frostnova.cli.idx.sync.config.ObjectMappers.yaml;
import static ch.frostnova.cli.idx.sync.io.FileSystemUtil.traverseAll;
import static ch.frostnova.cli.idx.sync.util.Invocation.runUnchecked;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isHidden;
import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.isRegularFile;
import static java.util.stream.Collectors.toSet;

/**
 * Task which scans the file system for {@link IdxSyncFile}s.
 */
public class FindSyncFilesTask implements Task<Map<IdxSyncFile, Path>> {

    private final static Set<Path> ignored = Stream.of("/dev", "/proc").map(Paths::get).collect(toSet());
    private volatile double progress;
    private String message;

    @Override
    public String getName() {
        return "Find sync files";
    }

    @Override
    public Map<IdxSyncFile, Path> run() {
        var maxRecurseDepth = 5;

        Map<IdxSyncFile, Path> result = new HashMap<>();
        var defaultExcludes = PathFilter.defaultExcludes();

        traverseAll(path -> {
            if (ignored.contains(path)) {
                return false;
            }
            if (path.getNameCount() > maxRecurseDepth) {
                return false;
            }
            if (isDirectory(path)) {
                if (defaultExcludes.test(path)) {
                    return false;
                }
                this.message = path.toString();
                var syncFilePath = path.resolve(FILENAME);
                if (isRegularFile(syncFilePath) && runUnchecked(() -> isReadable(path))) {
                    try {
                        var url = syncFilePath.toUri().toURL();
                        var syncFile = yaml().readValue(url, IdxSyncFile.class);
                        result.put(syncFile, syncFilePath);
                    } catch (Exception ignored) {

                    }
                }
                return runUnchecked(() -> path.getParent() == null || !isHidden(path));
            }
            return true;
        }, progress -> this.progress = progress);
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
