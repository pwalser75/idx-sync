package ch.frostnova.cli.idx.sync.monitor.impl;

import ch.frostnova.cli.idx.sync.monitor.ProgressMonitor;
import ch.frostnova.cli.idx.sync.monitor.ProgressTimer;
import ch.frostnova.cli.idx.sync.util.TimeFormat;

import java.util.Objects;

import static ch.frostnova.cli.idx.sync.console.ConsoleTools.printDone;
import static ch.frostnova.cli.idx.sync.console.ConsoleTools.printProgress;

/**
 * Console implementation of a progress monitor.
 */
public class ConsoleProgressMonitor implements ProgressMonitor {

    private String taskName;
    private ProgressTimer progressTimer;

    private long startTimeSystemNs;

    @Override
    public void start(String taskName) {
        if (progressTimer != null) {
            throw new IllegalStateException("Progress already started");
        }
        progressTimer = new ProgressTimer();
        this.taskName = Objects.requireNonNull(taskName, "taskName is required");
        startTimeSystemNs = System.nanoTime();
    }

    @Override
    public void update(double progress, String message) {
        if (progressTimer == null) {
            throw new IllegalStateException("Progress needs to be started first");
        }
        progressTimer.progress(progress);
        printProgress(progress, taskName, String.format(" %s %s", progressTimer, message));
    }

    @Override
    public void done(String message) {
        if (progressTimer == null) {
            throw new IllegalStateException("Progress needs to be started first");
        }
        progressTimer = null;

        long nanoTimeNow = System.nanoTime();
        double elapsedSec = 1e-9 * (nanoTimeNow - startTimeSystemNs);

        printDone(taskName, message + " (in " + TimeFormat.formatTime(elapsedSec) + ")");
    }

}
