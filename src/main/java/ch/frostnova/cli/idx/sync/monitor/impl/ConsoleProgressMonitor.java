package ch.frostnova.cli.idx.sync.monitor.impl;

import ch.frostnova.cli.idx.sync.monitor.ProgressMonitor;
import ch.frostnova.cli.idx.sync.util.TimeFormat;

import static ch.frostnova.cli.idx.sync.console.ConsoleTools.printDone;
import static ch.frostnova.cli.idx.sync.console.ConsoleTools.printProgress;
import static ch.frostnova.cli.idx.sync.util.TimeFormat.formatTime;

/**
 * Console implementation of a progress monitor.
 */
public class ConsoleProgressMonitor implements ProgressMonitor {

    private volatile long startTimeSystemNs;

    private volatile long lastProgressTimeNs;
    private volatile double progress;
    private volatile double movingAverageProgressPerNs;
    private volatile long etaTimeNs;


    public ConsoleProgressMonitor() {
        startTimeSystemNs = System.nanoTime();
        lastProgressTimeNs = startTimeSystemNs;
    }

    @Override
    public void update(String taskName, double progress, String message) {

        long nanoTimeNow = System.nanoTime();
        double elapsedSec = 1e-9 * (nanoTimeNow - startTimeSystemNs);

        double deltaProgress = progress - this.progress;
        double elapsedTimeNs = (nanoTimeNow - startTimeSystemNs);
        double deltaTimeNs = (nanoTimeNow - lastProgressTimeNs);
        double weight = deltaTimeNs / elapsedTimeNs;
        if (deltaTimeNs > 0) {
            double progressPerSec = deltaProgress / deltaTimeNs;
            movingAverageProgressPerNs = movingAverageProgressPerNs * (1 - weight) + progressPerSec * (weight);

            movingAverageProgressPerNs = progress / elapsedTimeNs;

            long remainingTimeNs = (long) ((1 - progress) / movingAverageProgressPerNs);
            etaTimeNs = nanoTimeNow + remainingTimeNs;
        }
        lastProgressTimeNs = nanoTimeNow;
        this.progress = progress;

        if (etaTimeNs > 0 && progress < 1) {
            double remainingSec = Math.max(0, etaTimeNs - nanoTimeNow) * 1e-9;
            printProgress(progress, taskName, String.format(" %s/%s %s", formatTime(elapsedSec), formatTime(remainingSec), message));
        } else {
            printProgress(progress, taskName, String.format(" %s %s", formatTime(elapsedSec), message));
        }
    }

    @Override
    public void done(String taskName, String message) {

        long nanoTimeNow = System.nanoTime();
        double elapsedSec = 1e-9 * (nanoTimeNow - startTimeSystemNs);

        printDone(taskName, message + " (in " + TimeFormat.formatTime(elapsedSec) + ")");
    }
}
