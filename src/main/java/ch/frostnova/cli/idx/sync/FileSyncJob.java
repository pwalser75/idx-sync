package ch.frostnova.cli.idx.sync;

import java.nio.file.Path;

public class FileSyncJob {

    private final Path sourcePath;
    private final Path targetPath;
    private final SyncAction syncAction;
    private final long fileSize;
    private final String reason;

    public FileSyncJob(Path sourcePath, Path targetPath, SyncAction syncAction) {
        this(sourcePath, targetPath, syncAction, 0, null);
    }

    public FileSyncJob(Path sourcePath, Path targetPath, SyncAction syncAction, long fileSize) {
        this(sourcePath, targetPath, syncAction, fileSize, null);
    }

    public FileSyncJob(Path sourcePath, Path targetPath, SyncAction syncAction, long fileSize, String reason) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.syncAction = syncAction;
        this.fileSize = fileSize;
        this.reason = reason;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public Path getTargetPath() {
        return targetPath;
    }

    public SyncAction getSyncAction() {
        return syncAction;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(syncAction);
        builder.append(", source: ");
        builder.append(sourcePath);
        builder.append(", target: ");
        builder.append(targetPath);
        if (fileSize > 0) {
            builder.append(", ");
            builder.append(fileSize + " bytes");
        }
        if (reason != null) {
            builder.append(", ");
            builder.append(reason);
        }
        return builder.toString();
    }
}
