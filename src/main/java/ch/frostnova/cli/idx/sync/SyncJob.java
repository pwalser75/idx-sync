package ch.frostnova.cli.idx.sync;

import ch.frostnova.cli.idx.sync.filter.PathFilter;

import java.nio.file.Path;
import java.util.function.Predicate;

public class SyncJob {

    private final String name;
    private final Path source;
    private final Path target;
    private final Predicate<Path> excludeFilter;

    public SyncJob(String name, Path source, Path target, Predicate<Path> excludeFilter) {
        this.name = name;
        this.source = source;
        this.target = target;
        this.excludeFilter = excludeFilter != null ? excludeFilter : PathFilter.NONE;
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

    public Predicate<Path> getExcludeFilter() {
        return excludeFilter;
    }
}
