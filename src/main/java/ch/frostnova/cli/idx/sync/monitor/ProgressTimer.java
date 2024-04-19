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
        var nanoTimeNow = nanoTime();
        var deltaProgress = progress - lastProgress;
        var deltaTimeNs = nanoTimeNow - lastProgressTime;

        if (deltaProgress > 0.01 && deltaTimeNs > 1e9) {

            var newEtaEndTimeNs = (long) (nanoTimeNow + deltaTimeNs * (1 - progress) / deltaProgress);
            var newEtaWeight = 0.5;
            etaEndTimeNs = etaEndTimeNs != null ? (long) (etaEndTimeNs * (1 - newEtaWeight) + newEtaEndTimeNs * newEtaWeight) : newEtaEndTimeNs;

            lastProgress = progress;
            lastProgressTime = nanoTimeNow;
        }
    }


    @Override
    public String toString() {

        var nanoTimeNow = nanoTime();

        var elapsedTimeSec = (nanoTimeNow - startTimeNs) * NANO;

        if (elapsedTimeSec < 1 || etaEndTimeNs == null) {
            return format("%s", formatTime(elapsedTimeSec));
        }
        var remainingTimeSec = Math.max(0, (etaEndTimeNs - nanoTimeNow) * NANO);
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
