package ch.frostnova.cli.idx.sync;

import ch.frostnova.cli.idx.sync.config.IdxSyncFile;
import ch.frostnova.cli.idx.sync.console.Console;
import ch.frostnova.cli.idx.sync.monitor.ProgressMonitor;
import ch.frostnova.cli.idx.sync.monitor.impl.ConsoleProgressMonitor;
import ch.frostnova.cli.idx.sync.task.BatchTask;
import ch.frostnova.cli.idx.sync.task.TaskRunner;
import ch.frostnova.cli.idx.sync.task.impl.CreateFileSyncJobsTask;
import ch.frostnova.cli.idx.sync.task.impl.FindSyncFilesTask;
import ch.frostnova.cli.idx.sync.task.impl.SyncFilesTask;

import java.io.Writer;
import java.nio.file.Path;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.frostnova.cli.idx.sync.config.IdxSyncFile.FILENAME;
import static ch.frostnova.cli.idx.sync.config.IdxSyncFile.resolve;
import static ch.frostnova.cli.idx.sync.config.ObjectMappers.yaml;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.*;
import static ch.frostnova.cli.idx.sync.console.Console.*;
import static ch.frostnova.cli.idx.sync.util.ByteFormat.formatBytes;
import static java.nio.file.Files.*;
import static java.util.Comparator.*;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class IdxSync {

    public static void main(String[] args) {
        new IdxSync(args);
    }

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
                Console.printf("%s %s: %s", error(), ex.getClass().getSimpleName(), ex.getMessage());
            } catch (Exception ex) {
                Console.printf("%s %s: %s", error(), ex.getClass().getSimpleName(), ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private static void printLogo() {
        Console.println(format("------------", ANSI_BLUE));
        Console.println(format(rocket() + " Idx SYNC", ANSI_BOLD, ANSI_BLUE));
        Console.println(format("------------", ANSI_BOLD, ANSI_BLUE));
    }

    private static void printUsage() {
        Console.printf("Usage: %s\n", format("java -jar idx-sync.jar [command] [args]...", ANSI_BOLD, ANSI_CYAN));
        Console.printf("Commands:\n");
        Console.printf("- %s: %s\n", format("run   ", ANSI_BOLD, ANSI_GREEN), "Synchronize files (default task when no arguments are provided)");
        Console.printf("- %s: %s\n", format("scan  ", ANSI_BOLD, ANSI_GREEN), "Scan for sync files and show matching pairs");
        Console.printf("- %s: %s\n", format("source", ANSI_BOLD, ANSI_GREEN), format("[path] [name]", ANSI_GRAY) + ": add the given path as a source with the given name");
        Console.printf("- %s: %s\n", format("target", ANSI_BOLD, ANSI_GREEN), format("[path] [source-folder-id]", ANSI_GRAY) + ": add the given path as a target for the source with the given id");
        Console.printf("- %s: %s\n", format("remove", ANSI_BOLD, ANSI_GREEN), format("[path]", ANSI_GRAY) + " remove the given path as source or target folder (deletes the .idxsync file)");
    }

    private List<SyncJob> scan() {

        List<SyncJob> result = new ArrayList<>();

        Comparator<IdxSyncFile> syncFileComparator = comparing(IdxSyncFile::getSourceFolderId, nullsFirst(naturalOrder()))
                .thenComparing(IdxSyncFile::getFolderName, nullsLast(Collator.getInstance()));

        Map<IdxSyncFile, Path> syncFilePaths = taskRunner.run(new FindSyncFilesTask());
        if (syncFilePaths.isEmpty()) {
            Console.printf("No %s files found.\n", FILENAME);
        } else {
            Console.printf("Found following %s files:\n", FILENAME);
            syncFilePaths.keySet().stream().sorted(syncFileComparator).forEach(syncFile -> {
                Path path = syncFilePaths.get(syncFile);
                if (syncFile.getSourceFolderId() != null) {
                    Console.printf("- %s %s: source = %s in %s\n", sync(), format(syncFile.getFolderId(), ANSI_BOLD, ANSI_CYAN), format(syncFile.getSourceFolderId(), ANSI_CYAN), path.getParent());
                } else {
                    Console.printf("- %s %s: %s, in %s\n", sync(), format(syncFile.getFolderId(), ANSI_BOLD, ANSI_CYAN), format(syncFile.getFolderName(), ANSI_CYAN), path.getParent());
                }
            });
        }

        Map<String, IdxSyncFile> syncFileById = syncFilePaths.keySet().stream().collect(toMap(IdxSyncFile::getFolderId, identity()));

        Map<IdxSyncFile, IdxSyncFile> syncSourceTarget = syncFilePaths.keySet().stream()
                .filter(it -> it.getSourceFolderId() != null)
                .filter(it -> syncFileById.containsKey(it.getSourceFolderId()))
                .collect(toMap(identity(), it -> syncFileById.get(it.getSourceFolderId())));

        if (syncSourceTarget.isEmpty()) {
            Console.println("No matching sync folders found.");
        } else {
            Console.println("Matching sync folders found:");
            syncSourceTarget.keySet().stream().sorted(syncFileComparator).forEach(target -> {
                IdxSyncFile source = syncSourceTarget.get(target);
                Console.printf("- %s %s %s -> %s\n", check(), format(source.getFolderName(), ANSI_BOLD, ANSI_GREEN), syncFilePaths.get(source).getParent(), syncFilePaths.get(target).getParent());
                result.add(new SyncJob(source.getFolderName(), syncFilePaths.get(source).getParent(), syncFilePaths.get(target).getParent()));
            });
        }

        return result;
    }

    private void run() {

        List<SyncJob> syncJobs = scan();

        BatchTask<List<FileSyncJob>> batchTask = new BatchTask<>("Comparing files", syncJobs.stream().map(CreateFileSyncJobsTask::new).collect(toList()));
        List<FileSyncJob> fileSyncJobs = taskRunner.run(batchTask).stream().flatMap(Collection::stream).collect(toList());

        SyncResult syncResult = taskRunner.run(new SyncFilesTask(fileSyncJobs));
        if (syncResult.getFilesCreated() > 0) {
            Console.printf("- %s files created\n", format(syncResult.getFilesCreated(), ANSI_BOLD, ANSI_GREEN));
        }
        if (syncResult.getFilesUpdated() > 0) {
            Console.printf("- %s files updated\n", format(syncResult.getFilesUpdated(), ANSI_BOLD, ANSI_BLUE));
        }
        if (syncResult.getFilesDeleted() > 0) {
            Console.printf("- %s files deleted\n", format(syncResult.getFilesDeleted(), ANSI_BOLD, ANSI_ORANGE));
        }
        if (syncResult.getBytesTransferred() > 0) {
            Console.printf("- %s transferred\n", format(formatBytes(syncResult.getBytesTransferred()), ANSI_BOLD, ANSI_YELLOW));
        }
        if (syncResult.getErrors().size() > 0) {
            Console.printf("- %s errors\n", format(syncResult.getErrors().size(), ANSI_BOLD, ANSI_RED));
            for (String error : syncResult.getErrors()) {
                Console.printf("  %s\n", format(error, ANSI_RED));
            }
        }
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
            Console.println("Created .idxsync file in " + path + ", folder id: " + syncFile.getFolderId());
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
            Console.println("Created .idxsync file in " + path);
        }
    }

    private static void remove(Path path) throws Exception {
        checkWriteableDir(path);
        Path syncFilePath = resolve(path);
        if (exists(syncFilePath)) {
            delete(syncFilePath);
            Console.println("Removed .idxsync file from " + path);
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
}
