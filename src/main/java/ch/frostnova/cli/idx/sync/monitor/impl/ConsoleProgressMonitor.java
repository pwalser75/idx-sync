package ch.frostnova.cli.idx.sync.monitor.impl;

import ch.frostnova.cli.idx.sync.console.ConsoleProgressBar;
import ch.frostnova.cli.idx.sync.monitor.ProgressMonitor;
import ch.frostnova.cli.idx.sync.monitor.ProgressTimer;
import ch.frostnova.cli.idx.sync.util.TimeFormat;

import static ch.frostnova.cli.idx.sync.console.ProgressBarStyle.ansi;
import static java.util.Objects.requireNonNull;

/**
 * Console implementation of a progress monitor.
 */
public class ConsoleProgressMonitor implements ProgressMonitor {

    private String taskName;
    private ProgressTimer progressTimer;
    private final ConsoleProgressBar consoleProgressBar;

    private long startTimeSystemNs;

    public ConsoleProgressMonitor() {
        this.consoleProgressBar = new ConsoleProgressBar(ansi());
    }

    @Override
    public void start(String taskName) {
        if (progressTimer != null) {
            throw new IllegalStateException("Progress already started");
        }
        this.taskName = requireNonNull(taskName, "taskName is required");
        progressTimer = new ProgressTimer();
        startTimeSystemNs = System.nanoTime();
    }

    @Override
    public void update(double progress, String message) {
        if (progressTimer == null) {
            throw new IllegalStateException("Progress needs to be started first");
        }
        progressTimer.progress(progress);
        consoleProgressBar.printProgress(progress, taskName, String.format(" %s %s", progressTimer, message));
    }

    @Override
    public void done(String message) {
        if (progressTimer == null) {
            throw new IllegalStateException("Progress needs to be started first");
        }
        progressTimer = null;

        long nanoTimeNow = System.nanoTime();
        double elapsedSec = 1e-9 * (nanoTimeNow - startTimeSystemNs);

        consoleProgressBar.printDone(taskName, message + " (in " + TimeFormat.formatTime(elapsedSec) + ")");
    }
}
