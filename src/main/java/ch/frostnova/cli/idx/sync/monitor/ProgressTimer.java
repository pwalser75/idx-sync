package ch.frostnova.cli.idx.sync.monitor;

import static ch.frostnova.cli.idx.sync.util.TimeFormat.formatTime;
import static java.lang.Math.round;
import static java.lang.String.format;
import static java.lang.System.nanoTime;

/**
 * A progress timer which can report the elapsed and estimated remaining time.
 */
public class ProgressTimer {

    private final static double NANO = 1e-9;

    private final long startTimeNs;

    private double lastProgress;
    private long lastProgressTime;

    private Long etaEndTimeNs;

    public ProgressTimer() {
        this.startTimeNs = nanoTime();
        lastProgressTime = startTimeNs;
    }

    public void progress(double progress) {
        long nanoTimeNow = nanoTime();
        double deltaProgress = progress - lastProgress;
        long deltaTimeNs = nanoTimeNow - lastProgressTime;

        if (deltaProgress > 0.01 || deltaTimeNs > 5e9) {

            long newEtaEndTimeNs = (long) (nanoTimeNow + deltaTimeNs * (1 - progress) / deltaProgress);
            double newEtaWeight = 0.2;
            etaEndTimeNs = etaEndTimeNs != null ? (long) (etaEndTimeNs * (1 - newEtaWeight) + newEtaEndTimeNs * newEtaWeight) : newEtaEndTimeNs;

            lastProgress = progress;
            lastProgressTime = nanoTimeNow;
        }
    }


    @Override
    public String toString() {

        long nanoTimeNow = nanoTime();

        double elapsedTimeSec = (nanoTimeNow - startTimeNs) * NANO;

        if (elapsedTimeSec < 1 || etaEndTimeNs == null) {
            return format("%s", formatTime(elapsedTimeSec));
        }
        double remainingTimeSec = Math.max(0, (etaEndTimeNs - nanoTimeNow) * NANO);
        if (remainingTimeSec < 1) {
            return format("%s", formatTime(elapsedTimeSec));
        }
        int roundSeconds;
        if (remainingTimeSec > 6000) {
            roundSeconds = 300;
        } else if (remainingTimeSec > 1200) {
            roundSeconds = 60;
        } else if (remainingTimeSec > 600) {
            roundSeconds = 30;
        } else if (remainingTimeSec > 90) {
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
