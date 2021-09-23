package ch.frostnova.cli.idx.sync.console;

import ch.frostnova.cli.idx.sync.util.Invocation;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static ch.frostnova.cli.idx.sync.console.AnsiEscape.*;

public final class ConsoleTools {

    private static final Terminal terminal;

    static {
        terminal = Invocation.runUnchecked(() ->
                TerminalBuilder.builder().color(true).encoding(StandardCharsets.UTF_8).build());
    }

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
        return Math.max(80, terminal.getWidth());
    }
}
