package ch.frostnova.cli.idx.sync.task.impl;

import ch.frostnova.cli.idx.sync.FileSyncJob;
import ch.frostnova.cli.idx.sync.SyncJob;
import ch.frostnova.cli.idx.sync.config.IdxSyncFile;
import ch.frostnova.cli.idx.sync.task.Task;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static ch.frostnova.cli.idx.sync.SyncAction.*;
import static ch.frostnova.cli.idx.sync.io.FileSystemUtil.traverse;
import static ch.frostnova.cli.idx.sync.util.Invocation.runUnchecked;
import static java.nio.file.Files.*;
import static java.time.Duration.between;

/**
 * Task which scans the file system for {@link IdxSyncFile}s.
 */
public class CreateFileSyncJobsTask implements Task<List<FileSyncJob>> {

    private final SyncJob syncJob;
    private double progress;
    private String message;

    public CreateFileSyncJobsTask(SyncJob syncJob) {
        this.syncJob = syncJob;
    }

    @Override
    public String getName() {
        return "Create jobs for " + syncJob.getName();
    }

    @Override
    public List<FileSyncJob> run() {
        List<FileSyncJob> result = new ArrayList<>();

        Set<Path> relativePaths = new HashSet<>();

        traverse(syncJob.getSource(), (path, progress) -> {
            this.progress = progress * 0.25;
            this.message = path.toString();
            Path relativePath = syncJob.getSource().relativize(path);
            boolean skip = skip(relativePath);
            if (!runUnchecked(() -> isHidden(path)) && !skip) {
                relativePaths.add(relativePath);
                return true;
            }
            return !isDirectory(path) || !skip;
        });
        traverse(syncJob.getTarget(), (path, progress) -> {
            this.progress = 0.25 + progress * 0.25;
            this.message = path.toString();
            Path relativePath = syncJob.getTarget().relativize(path);
            boolean skip = skip(relativePath);
            if (!runUnchecked(() -> isHidden(path)) && !skip) {
                relativePaths.add(relativePath);
                return true;
            }
            return !isDirectory(path) || !skip;
        });
        int index = 0;
        for (Path relativePath : relativePaths) {
            this.progress = 0.5 + (double) index / relativePaths.size() * 0.5;
            this.message = relativePath.toString();
            Path sourcePath = syncJob.getSource().resolve(relativePath);
            Path targetPath = syncJob.getTarget().resolve(relativePath);
            if (!exists(targetPath) && isRegularFile(sourcePath) && isReadable(sourcePath)) {
                result.add(new FileSyncJob(sourcePath, targetPath, CREATE, runUnchecked(() -> size(sourcePath))));
            } else if (!exists(sourcePath)) {
                result.add(new FileSyncJob(sourcePath, targetPath, DELETE));
            } else if (isRegularFile(sourcePath) && isReadable(sourcePath)) {
                Long sourceFileSize = runUnchecked(() -> size(sourcePath));
                Long targetFileSize = runUnchecked(() -> size(targetPath));

                BasicFileAttributes sourceAttributes = runUnchecked(() -> readAttributes(sourcePath, BasicFileAttributes.class));
                BasicFileAttributes targetAttributes = runUnchecked(() -> readAttributes(targetPath, BasicFileAttributes.class));

                Instant sourceLastModified = sourceAttributes.lastModifiedTime().toInstant();
                Instant targetLastModified = targetAttributes.lastModifiedTime().toInstant();
                Duration deltaTime = between(sourceLastModified, targetLastModified);

                if (!Objects.equals(sourceFileSize, targetFileSize) || deltaTime.compareTo(Duration.ofSeconds(1)) > 0) {
                    String info = String.format("%s: %s?=%s, %s?=%s, %s", relativePath,
                            sourceFileSize, targetFileSize, sourceLastModified, targetLastModified, deltaTime);
                    result.add(new FileSyncJob(sourcePath, targetPath, UPDATE, sourceFileSize, info));
                }
            }
            index++;
        }
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

    private static boolean skip(Path relativePath) {
        if (relativePath.equals(IdxSyncFile.FILENAME)) {
            return true;
        }
        return relativePath.equals(Path.of("$RECYCLE.BIN"));
    }
}
