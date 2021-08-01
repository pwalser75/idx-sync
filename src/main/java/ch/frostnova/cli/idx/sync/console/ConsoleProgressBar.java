package ch.frostnova.cli.idx.sync.console;

import static ch.frostnova.cli.idx.sync.console.AnsiEscape.*;
import static ch.frostnova.cli.idx.sync.console.ConsoleTools.*;

public class ConsoleProgressBar {

    private final ProgressBarStyle progressBarStyle;

    public ConsoleProgressBar(ProgressBarStyle progressBarStyle) {
        this.progressBarStyle = progressBarStyle;
    }

    public  void printProgress(int i, int n, String task, String message) {
        printProgress((double) i / n, task, message);
    }

    public  void printProgress(double progress, String task, String message) {
        task = task != null ? removeNonPrintableCharacters(task) : "";
        message = message != null ? removeNonPrintableCharacters(message) : "";

        int ticks = (int) (progress * progressBarStyle.getBarLength());
        int tickFraction = (int) ((progress * progressBarStyle.getBarLength() - ticks) * progressBarStyle.getProgressFractions().length());

        String bar = String.valueOf(progressBarStyle.getProgressBlock()).repeat(ticks);
        if (tickFraction > 0) {
            bar += progressBarStyle.getProgressFractions().charAt(tickFraction);
        }
        bar += " ".repeat(progressBarStyle.getBarLength() - printableSize(bar));
        String progressBar = String.format(" %s%s%s %.1f%% ", progressBarStyle.getLeftBracket(), bar, progressBarStyle.getRightBracket(), progress * 100);
        task = clip(task, progressBarStyle.getLineLength() - printableSize(progressBar));

        String line = task + progressBar;
        line += clip(message, progressBarStyle.getLineLength() - printableSize(line));
        line += " ".repeat(progressBarStyle.getLineLength() - printableSize(line));
        System.out.printf("\r%s%s", line, CLEAR_FROM_CURSOR);
    }

    public  void printDone(String task, String message) {
        System.out.println(format(task, CURSOR_START_LINE, CLEAR_FROM_CURSOR, ANSI_BOLD) + ": " + message);
    }
}
