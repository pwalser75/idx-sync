package ch.frostnova.cli.idx.sync;

import java.nio.file.Path;

public class FileSyncJob {

    private final static double FIXED_FILE_COST = 1e-4;
    private final static double FILE_COST_PER_BYTE = 2.75e-9;

    private final Path relativePath;
    private final SyncAction syncAction;
    private final long fileSize;
    private final String reason;

    public FileSyncJob(Path relativePath, SyncAction syncAction) {
        this(relativePath, syncAction, 0, null);
    }

    public FileSyncJob(Path relativePath, SyncAction syncAction, long fileSize) {
        this(relativePath, syncAction, fileSize, null);
    }

    public FileSyncJob(Path relativePath, SyncAction syncAction, long fileSize, String reason) {
        this.relativePath = relativePath;
        this.syncAction = syncAction;
        this.fileSize = fileSize;
        this.reason = reason;
    }

    public Path getRelativePath() {
        return relativePath;
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
        builder.append(", ");
        builder.append(relativePath);
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
