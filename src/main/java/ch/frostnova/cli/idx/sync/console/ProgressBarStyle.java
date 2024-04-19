package ch.frostnova.cli.idx.sync.console;

import java.util.Locale;

import static ch.frostnova.cli.idx.sync.console.AnsiEscape.ANSI_RESET;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.ANSI_YELLOW;

public class ProgressBarStyle {

    private final String leftBracket;
    private final String rightBracket;
    private final char progressBlock;
    private final String progressFractions;

    public ProgressBarStyle(String leftBracket, String rightBracket, char progressBlock, String progressFractions) {
        this.leftBracket = leftBracket;
        this.rightBracket = rightBracket;
        this.progressBlock = progressBlock;
        this.progressFractions = progressFractions;
    }

    public static void main(String[] args) {
        var style = ProgressBarStyle.autodetect();
        var consoleProgressBar = new ConsoleProgressBar(style);
        var n = 531;
        for (var i = 0; i <= n; i++) {
            consoleProgressBar.printProgress(i, n, "Test", i + "/" + n);
            try {
                Thread.sleep(5000 / n);
            } catch (Exception ignored) {
            }
        }
        consoleProgressBar.printDone("Test", "done");
    }

    public static ProgressBarStyle autodetect() {
        var operatingSystem = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
        if (operatingSystem.contains("linux") || operatingSystem.contains("unix") || operatingSystem.contains("mac")) {
            return ansi();
        }
        return plain();
    }

    public static ProgressBarStyle ansi() {
        return new ProgressBarStyle(ANSI_YELLOW + "│", "│" + ANSI_RESET, '\u2588', " \u258F\u258E\u258D\u258C\u258B\u258A\u2589\u2588");
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
