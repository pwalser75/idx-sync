package ch.frostnova.cli.idx.sync;

import ch.frostnova.cli.idx.sync.config.IdxSyncFile;
import ch.frostnova.cli.idx.sync.filter.PathFilter;
import ch.frostnova.cli.idx.sync.monitor.ProgressMonitor;
import ch.frostnova.cli.idx.sync.monitor.impl.ConsoleProgressMonitor;
import ch.frostnova.cli.idx.sync.task.BatchTask;
import ch.frostnova.cli.idx.sync.task.TaskRunner;
import ch.frostnova.cli.idx.sync.task.impl.CompareFilesTask;
import ch.frostnova.cli.idx.sync.task.impl.FindSyncFilesTask;
import ch.frostnova.cli.idx.sync.task.impl.SyncFilesTask;
import ch.frostnova.cli.idx.sync.util.ByteFormat;

import java.io.Writer;
import java.nio.file.Path;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.frostnova.cli.idx.sync.SyncAction.*;
import static ch.frostnova.cli.idx.sync.config.IdxSyncFile.FILENAME;
import static ch.frostnova.cli.idx.sync.config.IdxSyncFile.resolve;
import static ch.frostnova.cli.idx.sync.config.ObjectMappers.yaml;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.*;
import static ch.frostnova.cli.idx.sync.console.TextIcon.*;
import static ch.frostnova.cli.idx.sync.util.ByteFormat.formatBytes;
import static java.nio.file.Files.*;
import static java.util.Comparator.*;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public class IdxSync {

    private final ProgressMonitor progressMonitor = new ConsoleProgressMonitor();
    private final TaskRunner taskRunner = new TaskRunner(progressMonitor);

    public IdxSync(String[] args) {
        printLogo();

        if (args.length == 0) {
            run();
        } else {
            try {
                String command = args[0];
                if (command.equals("run") && args.length == 1) {
                    run();
                } else if (command.equals("scan") && args.length == 1) {
                    scan();
                } else if (command.equals("diff") && args.length == 1) {
                    diff(scan());
                } else if (command.equals("source") && args.length == 3) {
                    source(Path.of(args[1]), args[2]);
                } else if (command.equals("target") && args.length == 3) {
                    target(Path.of(args[1]), args[2]);
                } else if (command.equals("remove") && args.length == 2) {
                    remove(Path.of(args[1]));
                } else {
                    printUsage();
                }
            } catch (IllegalArgumentException ex) {
                System.out.print(String.format("%s %s: %s", ERROR, ex.getClass().getSimpleName(), ex.getMessage()));
            } catch (Exception ex) {
                System.out.print(String.format("%s %s: %s", ERROR, ex.getClass().getSimpleName(), ex.getMessage()));
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new IdxSync(args);
    }

    private static void printLogo() {
        System.out.println(format("------------", ANSI_BLUE));
        System.out.println(format(ROCKET + " Idx SYNC", ANSI_BOLD, ANSI_BLUE));
        System.out.println(format("------------", ANSI_BOLD, ANSI_BLUE));
    }

    private static void printUsage() {
        System.out.print(String.format("Usage: %s\n", format("java -jar idx-sync-shadow.jar [command] [args]...", ANSI_BOLD, ANSI_CYAN)));
        System.out.print(String.format("Commands:\n"));
        System.out.print(String.format("- %s: %s\n", format("run   ", ANSI_BOLD, ANSI_GREEN), "Synchronize files (default task when no arguments are provided)"));
        System.out.print(String.format("- %s: %s\n", format("scan  ", ANSI_BOLD, ANSI_GREEN), "Scan for sync files and show matching pairs"));
        System.out.print(String.format("- %s: %s\n", format("diff  ", ANSI_BOLD, ANSI_GREEN), "Scan for sync files, compare matching pairs and report changes"));
        System.out.print(String.format("- %s: %s\n", format("source", ANSI_BOLD, ANSI_GREEN), format("[path] [name]", ANSI_GRAY) + ": add the given path as a source with the given name"));
        System.out.print(String.format("- %s: %s\n", format("target", ANSI_BOLD, ANSI_GREEN), format("[path] [source-folder-id]", ANSI_GRAY) + ": add the given path as a target for the source with the given id"));
        System.out.print(String.format("- %s: %s\n", format("remove", ANSI_BOLD, ANSI_GREEN), format("[path]", ANSI_GRAY) + " remove the given path as source or target folder (deletes the .idxsync file)"));
    }

    private static Predicate<Path> createExcludeFilter(Set<String> excludeFilePatterns) {
        Predicate<Path> defaultExcludes = PathFilter.defaultExcludes();
        Predicate<Path> excludes = PathFilter.anyOf(excludeFilePatterns);
        return defaultExcludes.or(excludes);
    }

    private static void source(Path path, String name) throws Exception {
        checkWriteableDir(path);
        IdxSyncFile syncFile = new IdxSyncFile();
        syncFile.setFolderName(name);
        syncFile.setFolderId(UUID.randomUUID().toString());
        syncFile.setIncludeHidden(true);
        syncFile.setExcludePatterns(Stream.of(
                "**/node-modules",
                "**/.git"
        ).collect(Collectors.toSet()));
        try (Writer writer = newBufferedWriter(resolve(path))) {
            yaml().writeValue(writer, syncFile);
            System.out.println("Created .idxsync file in " + path + ", folder id: " + syncFile.getFolderId());
        }
    }

    private static void target(Path path, String sourceFolderId) throws Exception {
        createDirectories(path);
        checkWriteableDir(path);
        IdxSyncFile syncFile = new IdxSyncFile();
        syncFile.setFolderId(UUID.randomUUID().toString());
        syncFile.setSourceFolderId(sourceFolderId);
        try (Writer writer = newBufferedWriter(resolve(path))) {
            yaml().writeValue(writer, syncFile);
            System.out.println("Created .idxsync file in " + path);
        }
    }

    private static void remove(Path path) throws Exception {
        checkWriteableDir(path);
        Path syncFilePath = resolve(path);
        if (exists(syncFilePath)) {
            delete(syncFilePath);
            System.out.println("Removed .idxsync file from " + path);
        }
    }

    private static void checkWriteableDir(Path path) {
        if (!exists(path)) {
            throw new IllegalArgumentException(path + " does not exist");
        }
        if (!isDirectory(path)) {
            throw new IllegalArgumentException(path + " is not a directory");
        }
        if (!isWritable(path)) {
            throw new IllegalArgumentException(path + " is not writeable");
        }
    }

    private List<SyncJob> scan() {

        List<SyncJob> result = new ArrayList<>();

        Comparator<IdxSyncFile> syncFileComparator = comparing(IdxSyncFile::getSourceFolderId, nullsFirst(naturalOrder()))
                .thenComparing(IdxSyncFile::getFolderName, nullsLast(Collator.getInstance()));

        Map<IdxSyncFile, Path> syncFilePaths = new ConcurrentHashMap<>(taskRunner.run(new FindSyncFilesTask()));
        if (syncFilePaths.isEmpty()) {
            System.out.print(String.format("No %s files found.\n", FILENAME));
        } else {
            System.out.print(String.format("Found following %s files:\n", FILENAME));
            syncFilePaths.keySet().stream().sorted(syncFileComparator).forEach(syncFile -> {
                Path path = syncFilePaths.get(syncFile);
                if (syncFile.getSourceFolderId() != null) {
                    System.out.print(String.format("- %s %s: source = %s in %s\n", SYNC, format(syncFile.getFolderId(), ANSI_BOLD, ANSI_CYAN), format(syncFile.getSourceFolderId(), ANSI_CYAN), path.getParent()));
                } else {
                    System.out.print(String.format("- %s %s: %s, in %s\n", SYNC, format(syncFile.getFolderId(), ANSI_BOLD, ANSI_CYAN), format(syncFile.getFolderName(), ANSI_CYAN), path.getParent()));
                }
            });
        }

        Map<String, IdxSyncFile> syncFileById = syncFilePaths.keySet().stream().collect(toMap(IdxSyncFile::getFolderId, identity()));

        Map<IdxSyncFile, IdxSyncFile> syncSourceTarget = syncFilePaths.keySet().stream()
                .filter(it -> it.getSourceFolderId() != null)
                .filter(it -> syncFileById.containsKey(it.getSourceFolderId()))
                .collect(toMap(identity(), it -> syncFileById.get(it.getSourceFolderId())));

        if (syncSourceTarget.isEmpty()) {
            System.out.println("No matching sync folders found.");
        } else {
            System.out.println("Matching sync folders found:");
            syncSourceTarget.keySet().stream().sorted(syncFileComparator).forEach(target -> {
                IdxSyncFile source = syncSourceTarget.get(target);

                Set<String> excludePatterns = new HashSet<>();
                target.getExcludePatterns();
                excludePatterns.addAll(source.getExcludePatterns());
                excludePatterns.addAll(target.getExcludePatterns());
                Predicate<Path> excludeFilter = createExcludeFilter(excludePatterns);

                System.out.print(String.format("- %s %s %s -> %s\n", CHECK, format(source.getFolderName(), ANSI_BOLD, ANSI_GREEN), syncFilePaths.get(source).getParent(), syncFilePaths.get(target).getParent()));
                result.add(new SyncJob(source.getFolderName(), syncFilePaths.get(source).getParent(), syncFilePaths.get(target).getParent(), excludeFilter));
            });
        }

        return result;
    }

    private List<FileSyncJob> diff(List<SyncJob> syncJobs) {
        BatchTask<List<FileSyncJob>> batchTask = new BatchTask<>("Comparing files", syncJobs.stream().map(CompareFilesTask::new).collect(toList()));
        List<FileSyncJob> fileSyncJobs = taskRunner.run(batchTask).stream().flatMap(Collection::stream).collect(toList());
        Map<SyncAction, Long> syncActionsPerType = fileSyncJobs.stream().collect(groupingBy(f -> f.getSyncAction(), counting()));

        List<String> changes = new LinkedList<>();
        Optional.ofNullable(syncActionsPerType.get(CREATE))
                .filter(n -> n > 0)
                .map(n -> String.format("%s files created", format(n, ANSI_BOLD, ANSI_GREEN)))
                .ifPresent(changes::add);


        Optional.ofNullable(syncActionsPerType.get(UPDATE))
                .filter(n -> n > 0)
                .map(n -> String.format("%s files updated", format(n, ANSI_BOLD, ANSI_BLUE)))
                .ifPresent(changes::add);

        Optional.ofNullable(syncActionsPerType.get(DELETE))
                .filter(n -> n > 0)
                .map(n -> String.format("%s files deleted", format(n, ANSI_BOLD, ANSI_ORANGE)))
                .ifPresent(changes::add);

        if (!changes.isEmpty()) {
            System.out.println("Changes since last sync: " + changes.stream().collect(joining(", ")));
            System.out.println();
            fileSyncJobs.stream().filter(f -> f.getSyncAction() == CREATE).forEach(f -> {
                String output = String.format("+ %s\n  -> %s, %s", f.getSourcePath(), f.getTargetPath(), formatBytes(f.getFileSize()));
                System.out.println(format(output, ANSI_BOLD, ANSI_GREEN));
            });

            fileSyncJobs.stream().filter(f -> f.getSyncAction() == UPDATE).forEach(f -> {
                String output = String.format("* %s\n  -> %s, %s", f.getSourcePath(), f.getTargetPath(), formatBytes(f.getFileSize()));
                System.out.println(format(output, ANSI_BOLD, ANSI_BLUE));
            });

            fileSyncJobs.stream().filter(f -> f.getSyncAction() == DELETE).forEach(f -> {
                String output = String.format("- %s\n  -> %s, %s", f.getSourcePath(), f.getTargetPath(), formatBytes(f.getFileSize()));
                System.out.println(format(output, ANSI_BOLD, ANSI_ORANGE));
            });

        } else {
            System.out.println("No changes since last sync.");
        }
        return fileSyncJobs;
    }

    private SyncResult sync(List<FileSyncJob> fileSyncJobs) {
        return taskRunner.run(new SyncFilesTask(fileSyncJobs));
    }

    private void run() {

        List<SyncJob> syncJobs = scan();
        List<FileSyncJob> fileSyncJobs = diff(syncJobs);
        SyncResult syncResult = sync(fileSyncJobs);

        if (syncResult.isEmpty()) {
            System.out.println("Done, no actions performed.");
        } else {
            System.out.println("Sync Result:");
            if (syncResult.getFilesCreated() > 0) {
                System.out.print(String.format("- %s files created\n", format(syncResult.getFilesCreated(), ANSI_BOLD, ANSI_GREEN)));
            }
            if (syncResult.getFilesUpdated() > 0) {
                System.out.print(String.format("- %s files updated\n", format(syncResult.getFilesUpdated(), ANSI_BOLD, ANSI_BLUE)));
            }
            if (syncResult.getFilesDeleted() > 0) {
                System.out.print(String.format("- %s files deleted\n", format(syncResult.getFilesDeleted(), ANSI_BOLD, ANSI_ORANGE)));
            }
            if (syncResult.getBytesTransferred() > 0) {
                System.out.print(String.format("- %s transferred\n", format(formatBytes(syncResult.getBytesTransferred()), ANSI_BOLD, ANSI_YELLOW)));
            }
            if (syncResult.getErrors().size() > 0) {
                System.out.print(String.format("- %s errors\n", format(syncResult.getErrors().size(), ANSI_BOLD, ANSI_RED)));
                for (String error : syncResult.getErrors()) {
                    System.out.print(String.format("  %s\n", format(error, ANSI_RED)));
                }
            }
        }
    }
}
