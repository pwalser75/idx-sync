package ch.frostnova.cli.idx.sync.console;

public final class ConsoleTools {

    private static final int BAR_SIZE = 40;
    private static final int MAX_SIZE = 120;

    public static final String ANSI_RESET = "\u001b[0m";
    public static final String ANSI_BOLD = "\u001b[1m";
    public static final String ANSI_GRAY = "\u001b[90m";
    public static final String ANSI_YELLOW = "\u001b[33m";
    public static final String ANSI_GREEN = "\u001b[32m";
    public static final String ANSI_BLUE = "\u001b[34m";
    public static final String ANSI_CYAN = "\u001b[36m";

    private static final String CURSOR_START_LINE = "\u001b[1000D";
    private static final String CLEAR_FROM_CURSOR = "\u001b[0K";
    private static final String LEFT_BRACKET = ANSI_YELLOW + "│";
    private static final String RIGHT_BRACKET = "│" + ANSI_RESET;
    private static final char ELLIPSIS = '…';
    private static final char PROGRESS_BLOCK = '█';
    private static final String PROGRESS_FRACTIONS = " ▏▎▍▌▋▊▉█";

    private ConsoleTools() {
    }

    public static void clearLine() {
        System.out.print(CURSOR_START_LINE + CLEAR_FROM_CURSOR);
    }

    public static void printProgress(int i, int n, String task, String message) {
        printProgress((double) i / n, task, message);
    }

    public static void printProgress(double progress, String task, String message) {
        task = task != null ? removeNonPrintableCharacters(task) : "";
        message = message != null ? removeNonPrintableCharacters(message) : "";

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
        System.out.printf("\r%s%s", line, CLEAR_FROM_CURSOR);
    }

    public static void printDone(String task, String message) {
        System.out.println(CURSOR_START_LINE + CLEAR_FROM_CURSOR + ANSI_BOLD + task + ANSI_RESET + ": " + message);
    }

    public static int printableSize(String text) {
        return removeNonPrintableCharacters(text).length();
    }

    public static String removeNonPrintableCharacters(String text) {
        return text.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
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
