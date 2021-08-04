package ch.frostnova.cli.idx.sync.console;

import java.util.Locale;

import static ch.frostnova.cli.idx.sync.console.AnsiEscape.ANSI_RESET;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.ANSI_YELLOW;

public class ProgressBarStyle {

    private final String leftBracket;
    private final String rightBracket;
    private final char progressBlock;
    private final String progressFractions;

    public static void main(String[] args) {

        System.getProperties().forEach((k, v) -> System.out.println(k + " = " + v));


        ProgressBarStyle style = ProgressBarStyle.autodetect();
        ConsoleProgressBar consoleProgressBar = new ConsoleProgressBar(style);
        int n = 531;
        for (int i = 0; i <= n; i++) {
            consoleProgressBar.printProgress(i, n, "Test", i + "/" + n);
            try {
                Thread.sleep(5000 / n);
            } catch (Exception ignored) {
            }
        }
        consoleProgressBar.printDone("Test", "done");
    }

    public ProgressBarStyle(String leftBracket, String rightBracket, char progressBlock, String progressFractions) {
        this.leftBracket = leftBracket;
        this.rightBracket = rightBracket;
        this.progressBlock = progressBlock;
        this.progressFractions = progressFractions;
    }

    public static ProgressBarStyle autodetect() {
        String operatingSystem = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
        if (operatingSystem.contains("linux") || operatingSystem.contains("unix") || operatingSystem.contains("mac")) {
            return ansi();
        }
        return plain();
    }

    public static ProgressBarStyle ansi() {
        return new ProgressBarStyle(ANSI_YELLOW + "│", "│" + ANSI_RESET, '█', " ▏▎▍▌▋▊▉█");
    }

    public static ProgressBarStyle plain() {
        return new ProgressBarStyle("[", "]", '#', " ");
    }

    public String getLeftBracket() {
        return leftBracket;
    }

    public String getRightBracket() {
        return rightBracket;
    }

    public char getProgressBlock() {
        return progressBlock;
    }

    public String getProgressFractions() {
        return progressFractions;
    }
}
