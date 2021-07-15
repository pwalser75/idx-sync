package ch.frostnova.cli.idx.sync;

public final class ConsoleTools {

    private static final int BAR_SIZE = 50;
    private static final int MAX_SIZE = 120;

    private static final String LEFT_BRACKET = "\u001b[33m│";
    private static final String RIGHT_BRACKET = "│\u001b[0m";
    private static final char ELLIPSIS = '…';
    private static final char PROGRESS_BLOCK = '█';
    private static final String PROGRESS_FRACTIONS = " ▏▎▍▌▋▊▉█";

    private ConsoleTools() {
    }

    public static void printProgress(int i, int n, String task, String message) {
        printProgress((double) i / n, task, message);
    }

    public static void printProgress(double progress, String task, String message) {
        int ticks = (int) (progress * BAR_SIZE);
        int tickFraction = (int) ((progress * BAR_SIZE - ticks) * PROGRESS_FRACTIONS.length());

        String bar = String.valueOf(PROGRESS_BLOCK).repeat(ticks);
        if (tickFraction > 0) {
            bar += PROGRESS_FRACTIONS.charAt(tickFraction);
        }
        bar += " ".repeat(BAR_SIZE - printableSize(bar));
        String progressBar = String.format(" %s%s%s %.1f%% ", LEFT_BRACKET, bar, RIGHT_BRACKET, progress * 100);
        task = clip(task, MAX_SIZE - printableSize(progressBar));

        String line = task + progressBar;
        line += clip(message, MAX_SIZE - printableSize(line));
        line += " ".repeat(MAX_SIZE - printableSize(line));
        System.out.printf("\r%s", line);
    }

    public static int printableSize(String text) {
        return text.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "").length();
    }

    public static String clip(String text, int length) {
        if (length < 1) {
            return "";
        }
        if (printableSize(text) < length) {
            return text;
        }
        while (printableSize(text) > length - 1 && text.length() > 0) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ELLIPSIS;
    }
}
