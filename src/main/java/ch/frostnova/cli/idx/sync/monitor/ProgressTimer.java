package ch.frostnova.cli.idx.sync.monitor;

import static ch.frostnova.cli.idx.sync.util.TimeFormat.formatTime;

/**
 * A progress timer which can report the elapsed and estimated remaining time.
 */
public class ProgressTimer {

    private final double SPEED_WEIGHT = 0.5;

    private final long startTimeNs;

    private long etaEndTimeNs;

    private double movingAverageProgressPerNs;

    public ProgressTimer() {
        this.startTimeNs = System.nanoTime();
    }

    public void progress(double progress) {
        long nanoTimeNow = System.nanoTime();
        double elapsedTimeNs = nanoTimeNow - startTimeNs;
        double progressPerNs = elapsedTimeNs > 0 ? progress / elapsedTimeNs : 0;

        if (movingAverageProgressPerNs == 0) {
            movingAverageProgressPerNs = progressPerNs;
        } else {
            movingAverageProgressPerNs = movingAverageProgressPerNs * SPEED_WEIGHT + progressPerNs * (1 - SPEED_WEIGHT);
        }
        etaEndTimeNs = (long) (nanoTimeNow + (1 - progress) / movingAverageProgressPerNs);
    }

    public double getElapsedTimeSec() {
        long nanoTimeNow = System.nanoTime();
        return (nanoTimeNow - startTimeNs) * 1e-9;
    }

    public double getRemainingTimeSec() {
        long nanoTimeNow = System.nanoTime();
        return Math.max(0, (etaEndTimeNs - nanoTimeNow) * 1e-9);
    }

    @Override
    public String toString() {

        double elapsedTimeSec = getElapsedTimeSec();
        double remainingTimeSec = getRemainingTimeSec();

        if (elapsedTimeSec < 1) {
            return String.format("%s", formatTime(elapsedTimeSec));
        }
        int roundSeconds;
        if (remainingTimeSec > 90) {
            roundSeconds = 15;
        } else if (remainingTimeSec > 60) {
            roundSeconds = 10;
        } else if (remainingTimeSec > 15) {
            roundSeconds = 5;
        } else {
            roundSeconds = 1;
        }
        remainingTimeSec = roundSeconds * Math.round(remainingTimeSec / roundSeconds);
        return String.format("%s|%s", formatTime(elapsedTimeSec), formatTime(remainingTimeSec));
    }
}
