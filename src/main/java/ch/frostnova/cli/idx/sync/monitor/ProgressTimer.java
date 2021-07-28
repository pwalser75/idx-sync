package ch.frostnova.cli.idx.sync.monitor;

import ch.frostnova.cli.idx.sync.util.TimeFormat;

/**
 * A progress timer which can report the elapsed and estimated remaining time.
 */
public class ProgressTimer {

    private long startTimeNs;

    private long etaEndTimeNs;

    private double lastProgress;
    private double lastProgressTimeNs;
    private double movingAverageProgressPerNs;

    public ProgressTimer() {
        this.startTimeNs = System.nanoTime();
        lastProgress = 0;
        lastProgressTimeNs = startTimeNs;
    }

    public void progress(double progress) {
        long nanoTimeNow = System.nanoTime();
        double deltaProgress = progress - lastProgress;
        double deltaTimeNs = nanoTimeNow - lastProgressTimeNs;
        double elapsedTimeNs = nanoTimeNow - startTimeNs;

        double progressPerNs = deltaTimeNs > 0 ? deltaProgress / deltaTimeNs : 0;
        double weight = deltaTimeNs / elapsedTimeNs;
        movingAverageProgressPerNs = movingAverageProgressPerNs * (1 - weight) + progressPerNs * weight;
        etaEndTimeNs = (long) (nanoTimeNow + (1 - progress) / movingAverageProgressPerNs);

        lastProgress = progress;
        lastProgressTimeNs = nanoTimeNow;
    }

    @Override
    public String toString() {
        long nanoTimeNow = System.nanoTime();
        double elapsedSec = (nanoTimeNow - startTimeNs) * 1e-9;
        double remainingSec = (etaEndTimeNs - nanoTimeNow) * 1e-9;
        if (etaEndTimeNs <= nanoTimeNow) {
            return String.format("%s", TimeFormat.formatTime(elapsedSec));
        }
        int roundSeconds;
        if (remainingSec > 90) {
            roundSeconds = 15;
        } else if (remainingSec > 60) {
            roundSeconds = 10;
        } else if (remainingSec > 15) {
            roundSeconds = 5;
        } else {
            roundSeconds = 1;
        }
        remainingSec = roundSeconds * Math.round(remainingSec / roundSeconds);
        return String.format("%s|%s", TimeFormat.formatTime(elapsedSec), TimeFormat.formatTime(remainingSec));
    }
}
