package ch.frostnova.cli.idx.sync;

import ch.frostnova.cli.idx.sync.config.IdxSyncFile;
import ch.frostnova.cli.idx.sync.config.ObjectMappers;
import ch.frostnova.cli.idx.sync.monitor.ProgressMonitor;
import ch.frostnova.cli.idx.sync.monitor.impl.ConsoleProgressMonitor;
import ch.frostnova.cli.idx.sync.task.TaskRunner;
import ch.frostnova.cli.idx.sync.task.impl.CreateFileSyncJobsTask;
import ch.frostnova.cli.idx.sync.task.impl.FindSyncFilesTask;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.frostnova.cli.idx.sync.console.AnsiEscape.ANSI_BLUE;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.ANSI_BOLD;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.ANSI_CYAN;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.ANSI_GRAY;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.ANSI_GREEN;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class IdxSync {

    public static void main(String[] args) {
        new IdxSync(args);
    }

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
                System.out.printf("❌ %s: %s", ex.getClass().getSimpleName(), ex.getMessage());
            } catch (Exception ex) {
                System.out.printf("❌ %s: %s", ex.getClass().getSimpleName(), ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private static void printLogo() {
        System.out.println(format("------------", ANSI_BLUE));
        System.out.println(format("\uD83D\uDE80 Idx SYNC", ANSI_BOLD, ANSI_BLUE));
        System.out.println(format("------------", ANSI_BOLD, ANSI_BLUE));
    }

    private static void printUsage() {
        System.out.printf("Usage: %s\n", format("java -jar idx-sync-shadow.jar [command] [args]...", ANSI_BOLD, ANSI_CYAN));
        System.out.printf("Commands:\n");
        System.out.printf("- %s: %s\n", format("run   ", ANSI_BOLD, ANSI_GREEN), "Synchronize files (default task when no arguments are provided)");
        System.out.printf("- %s: %s\n", format("scan  ", ANSI_BOLD, ANSI_GREEN), "Scan for sync files and show matching pairs");
        System.out.printf("- %s: %s\n", format("source", ANSI_BOLD, ANSI_GREEN), format("[path] [name]", ANSI_GRAY) + ": add the given path as a source with the given name");
        System.out.printf("- %s: %s\n", format("target", ANSI_BOLD, ANSI_GREEN), format("[path] [source-folder-id]", ANSI_GRAY) + ": add the given path as a target for the source with the given id");
        System.out.printf("- %s: %s\n", format("remove", ANSI_BOLD, ANSI_GREEN), format("[path]", ANSI_GRAY) + " remove the given path as source or target folder (deletes the .idxsync file)");
    }

    private static void run() {
        ProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        TaskRunner taskRunner = new TaskRunner(progressMonitor);
        List<SyncJob> syncJobs = scan();
        List<FileSyncJob> fileSyncJobs = new ArrayList<>();
        for (SyncJob syncJob : syncJobs) {
            fileSyncJobs.addAll(taskRunner.run(new CreateFileSyncJobsTask(syncJob)));
        }
        for (FileSyncJob fileSyncJob : fileSyncJobs) {
            System.out.println("- " + fileSyncJob);
        }
    }

    private static List<SyncJob> scan() {
        ProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        TaskRunner taskRunner = new TaskRunner(progressMonitor);

        List<SyncJob> result = new ArrayList<>();

        Map<IdxSyncFile, Path> syncFilePaths = taskRunner.run(new FindSyncFilesTask());
        if (syncFilePaths.isEmpty()) {
            System.out.printf("No %s files found.\n", IdxSyncFile.FILENAME);
        } else {
            System.out.printf("Found following %s files:\n", IdxSyncFile.FILENAME);
            syncFilePaths.forEach((syncFile, path) -> {
                if (syncFile.getSourceFolderId() != null) {
                    System.out.printf("- \uD83D\uDD04 %s: source = %s in %s\n", format(syncFile.getFolderId(), ANSI_BOLD, ANSI_CYAN), format(syncFile.getSourceFolderId(), ANSI_CYAN), path);
                } else {
                    System.out.printf("- \uD83D\uDD04 %s: %s, in %s\n", format(syncFile.getFolderId(), ANSI_BOLD, ANSI_CYAN), format(syncFile.getFolderName(), ANSI_CYAN), path);
                }
            });
        }

        Map<String, IdxSyncFile> syncFileById = syncFilePaths.keySet().stream().collect(toMap(IdxSyncFile::getFolderId, identity()));

        Map<IdxSyncFile, IdxSyncFile> syncSourceTarget = syncFilePaths.keySet().stream()
                .filter(it -> it.getSourceFolderId() != null)
                .collect(toMap(identity(), it -> syncFileById.get(it.getSourceFolderId())));

        if (syncSourceTarget.isEmpty()) {
            System.out.println("No matching sync folders found.");
        } else {
            System.out.println("Matching sync folders found:");
            syncSourceTarget.forEach((target, source) -> {
                System.out.printf("- ✅ %s %s -> %s\n", format(source.getFolderName(), ANSI_BOLD, ANSI_GREEN), syncFilePaths.get(source).getParent(), syncFilePaths.get(target).getParent());
                result.add(new SyncJob(source.getFolderName(), syncFilePaths.get(source).getParent(), syncFilePaths.get(target).getParent()));
            });
        }

        return result;
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
        try (Writer writer = Files.newBufferedWriter(IdxSyncFile.resolve(path))) {
            ObjectMappers.yaml().writeValue(writer, syncFile);
            System.out.println("Created .idxsync file in " + path + ", folder id: " + syncFile.getFolderId());
        }
    }

    private static void target(Path path, String sourceFolderId) throws Exception {
        checkWriteableDir(path);
        IdxSyncFile syncFile = new IdxSyncFile();
        syncFile.setSourceFolderId(sourceFolderId);
        try (Writer writer = Files.newBufferedWriter(IdxSyncFile.resolve(path))) {
            ObjectMappers.yaml().writeValue(writer, syncFile);
            System.out.println("Created .idxsync file in " + path);
        }
    }

    private static void remove(Path path) throws Exception {
        checkWriteableDir(path);
        Path syncFilePath = IdxSyncFile.resolve(path);
        if (Files.exists(syncFilePath)) {
            Files.delete(syncFilePath);
            System.out.println("Removed .idxsync file from " + path);
        }
    }

    private static void checkWriteableDir(Path path) {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(path + " does not exist");
        }
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException(path + " is not a directory");
        }
        if (!Files.isWritable(path)) {
            throw new IllegalArgumentException(path + " is not writeable");
        }
    }
}
