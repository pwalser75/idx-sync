package ch.frostnova.cli.idx.sync.util;

public final class ByteFormat {

    private ByteFormat() {

    }

    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "-" + formatBytes(-bytes);
        }
        double value = bytes;
        if (value < 1000) {
            return String.format("%.0f %s", value, Unit.values()[0]);
        }
        for (var unit : Unit.values()) {
            if (value < 1000) {
                return String.format("%.1f %s", value, unit);
            }
            value /= 1000;
        }
        return String.format("%.1f %s", value * 1000, Unit.values()[Unit.values().length - 1]);
    }

    private enum Unit {

        BYTE("bytes"),
        KILOBYTE("kB"),
        MEGABYTE("MB"),
        GIGABYTE("GB"),
        TERABYTE("TB"),
        PETABYTE("PB");

        private String name;

        Unit(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
