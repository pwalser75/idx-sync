package ch.frostnova.cli.idx.sync.console;

import java.util.Locale;

import static ch.frostnova.cli.idx.sync.console.AnsiEscape.CLEAR_FROM_CURSOR;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.CURSOR_START_LINE;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.format;
import static ch.frostnova.cli.idx.sync.console.TextIcon.ELLIPSIS;

public final class Console {

    /**
     * Hard to get for different consoles and console windows sizes - as for now, we're using a static value.
     */
    private final static int LINE_LENGTH = 116;

    private Console() {
    }

    public static boolean isModern() {
        String operatingSystem = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
        return (operatingSystem.contains("linux") || operatingSystem.contains("unix") || operatingSystem.contains("mac"));
    }

    public static void clearLine() {
        Console.print(format("", CURSOR_START_LINE, CLEAR_FROM_CURSOR));
    }

    public static int printableSize(String text) {
        return removeNonPrintableCharacters(text).length();
    }

    public static String removeNonPrintableCharacters(String text) {
        return text.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
    }

    public static String removeAnsiEscapes(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }


    public static void println(String text) {
        print(text + "\n");
    }

    public static void printf(String text, Object... args) {
        print(String.format(text, args));
    }

    public static void print(String text) {
        if (!isModern()) {
            text = removeAnsiEscapes(text);
        }
        System.out.print(clip(text, getLineLength()));
    }

    public static String clip(String text, int length) {
        String ellipis = ELLIPSIS.toString();
        int elipsisSize = printableSize(ellipis);
        if (length < 1) {
            return "";
        }
        if (printableSize(text) <= length) {
            return text;
        }
        while (printableSize(text) > length - elipsisSize && text.length() > 0) {
            text = text.substring(0, text.length() - 1);
        }
        return text + ellipis;
    }

    private static String clearRight(String text, int length) {
        return text + " ".repeat(Math.max(0, length - printableSize(text)));
    }

    public static int getLineLength() {
        return LINE_LENGTH;
    }
}
