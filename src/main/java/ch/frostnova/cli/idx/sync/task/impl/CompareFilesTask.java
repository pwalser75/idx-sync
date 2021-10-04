package ch.frostnova.cli.idx.sync.task.impl;

import ch.frostnova.cli.idx.sync.FileSyncJob;
import ch.frostnova.cli.idx.sync.SyncJob;
import ch.frostnova.cli.idx.sync.config.IdxSyncFile;
import ch.frostnova.cli.idx.sync.task.Task;
import ch.frostnova.cli.idx.sync.util.Invocation;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

import static ch.frostnova.cli.idx.sync.SyncAction.*;
import static ch.frostnova.cli.idx.sync.io.FileSystemUtil.traverse;
import static java.nio.file.Files.*;
import static java.time.Duration.between;

/**
 * Task which scans the file system for {@link IdxSyncFile}s.
 */
public class CompareFilesTask implements Task<List<FileSyncJob>> {

    private final SyncJob syncJob;
    private double progress;
    private String message;

    public CompareFilesTask(SyncJob syncJob) {
        this.syncJob = syncJob;
    }

    @Override
    public String getName() {
        return "Create jobs for " + syncJob.getName();
    }

    @Override
    public List<FileSyncJob> run() {
        List<FileSyncJob> result = Collections.synchronizedList(new ArrayList<>());

        Predicate<Path> excludeFilter = syncJob.getExcludeFilter();

        Set<Path> relativePaths = new HashSet<>();
        Map<Path, Path> sourcePaths = new HashMap<>();
        Map<Path, Path> targetPaths = new HashMap<>();

        traverse(syncJob.getSource(), (path, progress) -> {
            this.progress = progress * 0.25;
            this.message = path.toString();
            Path relativePath = syncJob.getSource().relativize(path);
            if (excludeFilter.test(relativePath)) {
                return !isDirectory(path);
            }
            relativePaths.add(relativePath);
            sourcePaths.put(relativePath, path);
            return true;
        });
        traverse(syncJob.getTarget(), (path, progress) -> {
            this.progress = 0.25 + progress * 0.25;
            this.message = path.toString();
            Path relativePath = syncJob.getTarget().relativize(path);
            if (excludeFilter.test(relativePath)) {
                return !isDirectory(path);
            }
            relativePaths.add(relativePath);
            targetPaths.put(relativePath, path);
            return true;
        });
        double weight = 0.5 / relativePaths.size();
        relativePaths.stream().sorted().parallel().forEach(relativePath -> {
            Invocation.runUnchecked(() -> {
                this.message = relativePath.toString();
                Path sourcePath = sourcePaths.get(relativePath);
                Path targetPath = targetPaths.get(relativePath);
                if (targetPath == null && isRegularFile(sourcePath) && isReadable(sourcePath)) {
                    result.add(new FileSyncJob(sourcePath, syncJob.getTarget().resolve(relativePath), CREATE, size(sourcePath)));
                } else if (sourcePath == null) {
                    result.add(new FileSyncJob(syncJob.getSource().resolve(relativePath), targetPath, DELETE));
                } else if (isRegularFile(sourcePath) && isReadable(sourcePath)) {

                    BasicFileAttributes sourceAttributes = readAttributes(sourcePath, BasicFileAttributes.class);
                    BasicFileAttributes targetAttributes = readAttributes(targetPath, BasicFileAttributes.class);
                    Long sourceFileSize = sourceAttributes.size();
                    Long targetFileSize = targetAttributes.size();

                    Instant sourceLastModified = sourceAttributes.lastModifiedTime().toInstant();
                    Instant targetLastModified = targetAttributes.lastModifiedTime().toInstant();
                    Duration deltaTime = between(sourceLastModified, targetLastModified);

                    if (!Objects.equals(sourceFileSize, targetFileSize) || deltaTime.compareTo(Duration.ofSeconds(1)) > 0) {
                        result.add(new FileSyncJob(sourcePath, targetPath, UPDATE, sourceFileSize));
                    }
                }
                this.progress += weight;
            });
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
