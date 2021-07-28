package ch.frostnova.cli.idx.sync;

import ch.frostnova.cli.idx.sync.config.IdxSyncFile;
import ch.frostnova.cli.idx.sync.monitor.ProgressMonitor;
import ch.frostnova.cli.idx.sync.monitor.impl.ConsoleProgressMonitor;
import ch.frostnova.cli.idx.sync.task.TaskRunner;
import ch.frostnova.cli.idx.sync.task.impl.FindSyncFilesTask;

import java.nio.file.Path;
import java.util.Map;

import static ch.frostnova.cli.idx.sync.console.ConsoleTools.ANSI_BLUE;
import static ch.frostnova.cli.idx.sync.console.ConsoleTools.ANSI_BOLD;
import static ch.frostnova.cli.idx.sync.console.ConsoleTools.ANSI_CYAN;
import static ch.frostnova.cli.idx.sync.console.ConsoleTools.ANSI_GREEN;
import static ch.frostnova.cli.idx.sync.console.ConsoleTools.ANSI_RESET;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class IdxSync {

    public static void main(String[] args) {
        new IdxSync();
    }

    public IdxSync() {
        System.out.println(ANSI_BOLD + ANSI_BLUE + "\uD83D\uDE80 Idx SYNC" + ANSI_RESET);

        ProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        TaskRunner taskRunner = new TaskRunner(progressMonitor);

        Map<IdxSyncFile, Path> syncFilePaths = taskRunner.run(new FindSyncFilesTask());
        if (syncFilePaths.isEmpty()) {
            System.out.printf("No %s files found.\n", IdxSyncFile.FILENAME);
        } else {
            System.out.printf("Found following %s files:\n", IdxSyncFile.FILENAME);
            syncFilePaths.forEach((syncFile, path) -> {
                if (syncFile.getSourceFolderId() != null) {
                    System.out.printf("- \uD83D\uDD04 %s: %s, source = %s in %s\n", ANSI_BOLD + ANSI_CYAN + syncFile.getFolderId() + ANSI_RESET, syncFile.getFolderName(), syncFile.getSourceFolderId(), path);
                } else {
                    System.out.printf("- \uD83D\uDD04 %s: %s, in %s\n", ANSI_BOLD + ANSI_CYAN + syncFile.getFolderId() + ANSI_RESET, syncFile.getFolderName(), path);
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
            syncSourceTarget.forEach((target, source) -> System.out.printf("- ✅ %s %s -> %s\n",
                    ANSI_BOLD + ANSI_GREEN + source.getFolderName() + ANSI_RESET, syncFilePaths.get(source), syncFilePaths.get(target)));
        }

    }
}
