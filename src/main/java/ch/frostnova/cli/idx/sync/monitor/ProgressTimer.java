package ch.frostnova.cli.idx.sync.monitor;

import ch.frostnova.cli.idx.sync.util.LimitedList;

import java.util.List;

import static ch.frostnova.cli.idx.sync.util.TimeFormat.formatTime;
import static java.lang.Math.round;
import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.util.stream.Collectors.averagingDouble;

/**
 * A progress timer which can report the elapsed and estimated remaining time.
 */
public class ProgressTimer {

    private final static double NANO = 1e-9;

    private final long startTimeNs;
    private final List<Double> lastProgressSamples = new LimitedList<>(20);

    private double lastProgress;
    private long lastProgressTime;

    private long etaEndTimeNs;


    public ProgressTimer() {
        this.startTimeNs = nanoTime();
        lastProgressTime = startTimeNs;
    }

    public void progress(double progress) {
        long nanoTimeNow = nanoTime();
        if (progress < lastProgress + 0.05) {
            return;
        }

        double deltaProgress = progress - lastProgress;
        long deltaTimeNs = nanoTimeNow - lastProgressTime;

        lastProgressSamples.add(deltaProgress / deltaTimeNs);
        double averageProgress = lastProgressSamples.stream().collect(averagingDouble(d -> d));
        etaEndTimeNs = (long) (nanoTimeNow + (1 - progress) / averageProgress);

        lastProgress = progress;
        lastProgressTime = nanoTimeNow;
    }

    public double getElapsedTimeSec() {
        long nanoTimeNow = nanoTime();
        return (nanoTimeNow - startTimeNs) * NANO;
    }

    public double getRemainingTimeSec() {
        long nanoTimeNow = nanoTime();
        return Math.max(0, (etaEndTimeNs - nanoTimeNow) * NANO);
    }

    @Override
    public String toString() {

        double elapsedTimeSec = getElapsedTimeSec();
        double remainingTimeSec = getRemainingTimeSec();

        if (elapsedTimeSec < 1 || remainingTimeSec < 1) {
            return format("%s", formatTime(elapsedTimeSec));
        }
        int roundSeconds;
        if (remainingTimeSec > 90) {
            roundSeconds = 15;
        } else if (remainingTimeSec > 60) {
            roundSeconds = 10;
        } else if (remainingTimeSec > 20) {
            roundSeconds = 5;
        } else {
            roundSeconds = 1;
        }
        remainingTimeSec = roundSeconds * round(remainingTimeSec / roundSeconds);
        return format("%s|%s", formatTime(elapsedTimeSec), formatTime(remainingTimeSec));
    }
}
