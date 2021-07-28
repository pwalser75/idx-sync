package ch.frostnova.cli.idx.sync;

import java.nio.file.Path;

public class FileSyncJob {

    private final static double FIXED_FILE_COST = 1e-4;
    private final static double FILE_COST_PER_BYTE = 2.75e-9;

    private final Path relativePath;
    private final SyncAction syncAction;
    private final long fileSize;

    public FileSyncJob(Path relativePath, SyncAction syncAction, long fileSize) {
        this.relativePath = relativePath;
        this.syncAction = syncAction;
        this.fileSize = fileSize;
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
}
