package ch.frostnova.cli.idx.sync;

import java.nio.file.Path;

public class FileSyncJob {

    private final Path sourcePath;
    private final Path targetPath;
    private final SyncAction syncAction;
    private final long fileSize;

    public FileSyncJob(Path sourcePath, Path targetPath, SyncAction syncAction) {
        this(sourcePath, targetPath, syncAction, 0);
    }

    public FileSyncJob(Path sourcePath, Path targetPath, SyncAction syncAction, long fileSize) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.syncAction = syncAction;
        this.fileSize = fileSize;
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

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(syncAction);
        builder.append(", source: ");
        builder.append(sourcePath);
        builder.append(", target: ");
        builder.append(targetPath);
        if (fileSize > 0) {
            builder.append(", ");
            builder.append(fileSize + " bytes");
        }
        return builder.toString();
    }
}
