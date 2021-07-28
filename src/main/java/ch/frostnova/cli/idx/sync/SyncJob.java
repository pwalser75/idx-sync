package ch.frostnova.cli.idx.sync;

import java.nio.file.Path;

public class SyncJob {

    private final String name;
    private final Path source;
    private final Path target;

    public SyncJob(String name, Path source, Path target) {
        this.name = name;
        this.source = source;
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public Path getSource() {
        return source;
    }

    public Path getTarget() {
        return target;
    }
}
