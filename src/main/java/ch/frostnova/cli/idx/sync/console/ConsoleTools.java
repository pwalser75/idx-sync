package ch.frostnova.cli.idx.sync.console;

import java.util.Locale;

import static ch.frostnova.cli.idx.sync.console.AnsiEscape.CLEAR_FROM_CURSOR;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.CURSOR_START_LINE;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.format;

public final class ConsoleTools {

    /**
     * Hard to get for different consoles and console windows sizes - as for now, we're using a static value.
     */
    private final static int LINE_LENGTH = 76;

    private ConsoleTools() {
    }

    public static boolean isModern() {
        String operatingSystem = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
        return (operatingSystem.contains("linux") || operatingSystem.contains("unix") || operatingSystem.contains("mac"));
    }

    public static String rocket() {
        return isModern() ? "\uD83D\uDE80" : "";
    }

    public static String ellipis() {
        return isModern() ? "\u2026" : "...";
    }

    public static String error() {
        return isModern() ? "\u274C" : "[x]";
    }

    public static String sync() {
        return isModern() ? "\uD83D\uDD04" : "<->";
    }

    public static String check() {
        return isModern() ? "\u2705" : "*";
    }

    public static void clearLine() {
        System.out.print(format("", CURSOR_START_LINE, CLEAR_FROM_CURSOR));
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
        return text + ellipis();
    }

    public static int getLineLength() {
        return LINE_LENGTH;
    }
}
