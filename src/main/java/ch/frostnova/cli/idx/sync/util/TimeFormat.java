package ch.frostnova.cli.idx.sync.util;

import static java.lang.String.format;

public final class TimeFormat {
    private TimeFormat() {

    }

    public static String formatTime(double timeSec) {
        long sec = (long) (timeSec + 0.5);
        long min = sec / 60;
        sec %= 60;
        long h = min / 60;
        min %= 60;
        if (h > 0) {
            return format("%02d:%02d:%02d", h, min, sec);
        }
        return format("%02d:%02d", min, sec);
    }
}
