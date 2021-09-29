package ch.frostnova.cli.idx.sync;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SyncResult {

    private long filesCreated;
    private long filesUpdated;
    private long filesDeleted;
    private long bytesTransferred;
    private List<String> errors;

    public SyncResult(long filesCreated, long filesUpdated, long filesDeleted, long bytesTransferred, List<String> errors) {
        this.filesCreated = filesCreated;
        this.filesUpdated = filesUpdated;
        this.filesDeleted = filesDeleted;
        this.bytesTransferred = bytesTransferred;
        this.errors = Optional.ofNullable(errors).orElseGet(LinkedList::new);
    }

    public long getFilesCreated() {
        return filesCreated;
    }

    public long getFilesUpdated() {
        return filesUpdated;
    }

    public long getFilesDeleted() {
        return filesDeleted;
    }

    public long getBytesTransferred() {
        return bytesTransferred;
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean isEmpty() {
        return filesCreated + filesUpdated + filesDeleted == 0 && errors.isEmpty();
    }
}
