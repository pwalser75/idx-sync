package ch.frostnova.cli.idx.sync.console;

import static ch.frostnova.cli.idx.sync.console.ConsoleTools.printProgress;
import static ch.frostnova.cli.idx.sync.util.TimeFormat.formatTime;

public class ConsoleProgressBar implements AutoCloseable {

    private final Thread progressUpdater;
    private final String taskName;
    private volatile boolean active = true;
    private volatile long startTimeSystemNs;

    private volatile long lastProgressTimeNs;
    private volatile double progress;
    private volatile String message;
    private volatile double movingAverageProgressPerNs;
    private volatile long etaTimeNs;

    public ConsoleProgressBar(String taskName) {
        this.taskName = taskName;
        startTimeSystemNs = System.nanoTime();
        lastProgressTimeNs = startTimeSystemNs;
        progressUpdater = new Thread(() -> {
            while (active) {
                printCurrentProgress();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        progressUpdater.setDaemon(true);
        progressUpdater.start();
    }

    public void setProgress(int i, int n, String message) {
        setProgress((double) i / n, message);
    }

    public void setProgress(double progress, String message) {
        long nanoTimeNow = System.nanoTime();
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
        this.message = message;
    }

    public void println(Object message) {
        ConsoleTools.clearLine();
        System.out.println(message);
        printCurrentProgress();
    }

    private void printCurrentProgress() {
        long nanoTimeNow = System.nanoTime();
        double elapsedSec = 1e-9 * (nanoTimeNow - startTimeSystemNs);
        if (etaTimeNs > 0 && progress < 1) {
            double remainingSec = Math.max(0, etaTimeNs - nanoTimeNow) * 1e-9;
            printProgress(progress, taskName, String.format(" %s/%s %s", formatTime(elapsedSec), formatTime(remainingSec), message));
        } else {
            printProgress(progress, taskName, String.format(" %s %s", formatTime(elapsedSec), message));
        }
    }

    @Override
    public void close() {
        if (!active) {
            return;
        }
        active = false;
        try {
            progressUpdater.join();
        } catch (InterruptedException ignored) {
        }
        progress = 1;
        message = "";
        printCurrentProgress();
        System.out.println();
    }
}
