package ch.frostnova.cli.idx.sync.console;

import static ch.frostnova.cli.idx.sync.console.AnsiEscape.ANSI_BOLD;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.CLEAR_FROM_CURSOR;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.CURSOR_START_LINE;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.format;
import static ch.frostnova.cli.idx.sync.console.Console.clip;
import static ch.frostnova.cli.idx.sync.console.Console.getLineLength;
import static ch.frostnova.cli.idx.sync.console.Console.isModern;
import static ch.frostnova.cli.idx.sync.console.Console.printableSize;
import static ch.frostnova.cli.idx.sync.console.Console.removeNonPrintableCharacters;

public class ConsoleProgressBar {

    private final ProgressBarStyle progressBarStyle;

    public ConsoleProgressBar(ProgressBarStyle progressBarStyle) {
        this.progressBarStyle = progressBarStyle;
    }

    public void printProgress(int i, int n, String task, String message) {
        printProgress((double) i / n, task, message);
    }

    public void printProgress(double progress, String task, String message) {

        int lineLength = getLineLength();
        int barLength = Math.max(5, lineLength * 3 / 10);

        task = task != null ? removeNonPrintableCharacters(task) : "";
        message = message != null ? removeNonPrintableCharacters(message) : "";

        int ticks = (int) (progress * barLength);
        int tickFraction = (int) ((progress * barLength - ticks) * progressBarStyle.getProgressFractions().length());

        String bar = String.valueOf(progressBarStyle.getProgressBlock()).repeat(ticks);
        if (tickFraction > 0) {
            bar += progressBarStyle.getProgressFractions().charAt(tickFraction);
        }
        bar += " ".repeat(barLength - printableSize(bar));
        String progressBar = String.format(" %s%s%s %.1f%% ", progressBarStyle.getLeftBracket(), bar, progressBarStyle.getRightBracket(), progress * 100);
        task = clip(task, lineLength - printableSize(progressBar));

        String line = task + progressBar;
        line += clip(message, lineLength - printableSize(line));
        line += " ".repeat(Math.max(0, lineLength - printableSize(line)));
        System.out.print(String.format("\r%s", line));
    }

    public static void printDone(String task, String message) {
        if (isModern()) {
            System.out.println(format(task, CURSOR_START_LINE, CLEAR_FROM_CURSOR, ANSI_BOLD) + ": " + message);
        } else {
            System.out.print(String.format("\r%s\r%s: %s\n", " ".repeat(getLineLength()), task, message));
        }
    }
}
