package ch.frostnova.cli.idx.sync.console;

public class ProgressBarStyle {

    private final int lineLength;
    private final int barLength;

    private final String leftBracket;
    private final String rightBracket;
    private final char progressBlock;
    private final String progressFractions;

    public static void main(String[] args) {
        ProgressBarStyle style = ProgressBarStyle.ansi();
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

    public ProgressBarStyle(int lineLength, int barLength, String leftBracket, String rightBracket, char progressBlock, String progressFractions) {
        this.lineLength = lineLength;
        this.barLength = barLength;
        this.leftBracket = leftBracket;
        this.rightBracket = rightBracket;
        this.progressBlock = progressBlock;
        this.progressFractions = progressFractions;
    }

    public static ProgressBarStyle ansi() {
        return new ProgressBarStyle(160, 40,
                AnsiEscape.ANSI_YELLOW + "│", "│" + AnsiEscape.ANSI_RESET,
                '█', " ▏▎▍▌▋▊▉█");
    }

    public static ProgressBarStyle plain() {
        return new ProgressBarStyle(160, 32,
                "[", "]",
                '#', " ");
    }

    public int getLineLength() {
        return lineLength;
    }

    public int getBarLength() {
        return barLength;
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
