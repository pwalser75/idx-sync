package ch.frostnova.cli.idx.sync.task.impl;

import ch.frostnova.cli.idx.sync.FileSyncJob;
import ch.frostnova.cli.idx.sync.SyncResult;
import ch.frostnova.cli.idx.sync.task.Task;
import ch.frostnova.cli.idx.sync.util.Invocation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static ch.frostnova.cli.idx.sync.SyncAction.CREATE;
import static ch.frostnova.cli.idx.sync.SyncAction.DELETE;
import static ch.frostnova.cli.idx.sync.SyncAction.UPDATE;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readAttributes;
import static java.nio.file.Files.setLastModifiedTime;
import static java.nio.file.Files.walkFileTree;
import static java.util.stream.Collectors.summarizingDouble;

/**
 * Task which synchronizes files.
 */
public class SyncFilesTask implements Task<SyncResult> {

    private final static double FIXED_FILE_COST = 1e-4;
    private final static double FILE_COST_PER_BYTE = 2.75e-9;

    private final List<FileSyncJob> syncJobs;
    private double progress;
    private String message;

    public SyncFilesTask(List<FileSyncJob> syncJobs) {
        this.syncJobs = Objects.requireNonNull(syncJobs, "syncJobs are required");
    }

    private static double getCost(FileSyncJob fileSyncJob) {
        return FIXED_FILE_COST + fileSyncJob.getFileSize() * FILE_COST_PER_BYTE;
    }

    private static void delete(Path path) {
        if (!exists(path)) {
            return;
        }
        Invocation.runUnchecked(() -> {
            walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return CONTINUE;
                }
            });
        });
    }

    @Override
    public String getName() {
        return "Synching files";
    }

    @Override
    public SyncResult run() {

        var totalCost = syncJobs.stream().collect(summarizingDouble(SyncFilesTask::getCost)).getSum();

        long filesCreated = 0;
        long filesUpdated = 0;
        long filesDeleted = 0;
        long bytesTransferred = 0;
        List<String> errors = new ArrayList<>();

        double costDone = 0;

        var buffer = new byte[0xFFFF];

        for (var fileSyncJob : syncJobs) {
            message = fileSyncJob.getSyncAction().name().toLowerCase(Locale.ROOT) + " " + fileSyncJob.getTargetPath();
            progress = costDone / totalCost;
            var cost = getCost(fileSyncJob);

            try {
                if (fileSyncJob.getSyncAction() == CREATE || fileSyncJob.getSyncAction() == UPDATE) {
                    var fileSize = fileSyncJob.getFileSize();
                    long transferred = 0;
                    createDirectories(fileSyncJob.getTargetPath().getParent());
                    try (InputStream in = new BufferedInputStream(newInputStream(fileSyncJob.getSourcePath()))) {
                        try (OutputStream out = new BufferedOutputStream(newOutputStream(fileSyncJob.getTargetPath()))) {
                            int read;
                            while ((read = in.read(buffer)) >= 0) {
                                out.write(buffer, 0, read);
                                transferred += read;
                                progress = (costDone + cost * transferred / fileSize) / totalCost;
                            }
                            out.flush();
                        }
                    }
                    bytesTransferred += transferred;
                    var sourceAttributes = readAttributes(fileSyncJob.getSourcePath(), BasicFileAttributes.class);
                    setLastModifiedTime(fileSyncJob.getTargetPath(), sourceAttributes.lastModifiedTime());
                    if (fileSyncJob.getSyncAction() == CREATE) {
                        filesCreated++;
                    } else {
                        filesUpdated++;
                    }
                } else if (fileSyncJob.getSyncAction() == DELETE) {
                    delete(fileSyncJob.getTargetPath());
                    filesDeleted++;
                }
            } catch (Exception ex) {
                errors.add(String.format("%s | %s: %s", message, ex.getClass().getSimpleName(), ex.getMessage()));
            }
            costDone += cost;
        }

        return new SyncResult(filesCreated, filesUpdated, filesDeleted, bytesTransferred, errors);
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
