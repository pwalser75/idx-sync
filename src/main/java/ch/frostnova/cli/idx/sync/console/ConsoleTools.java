package ch.frostnova.cli.idx.sync.console;

import ch.frostnova.cli.idx.sync.util.Invocation;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.nio.charset.StandardCharsets;

import static ch.frostnova.cli.idx.sync.console.AnsiEscape.CLEAR_FROM_CURSOR;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.CURSOR_START_LINE;
import static ch.frostnova.cli.idx.sync.console.AnsiEscape.format;

public final class ConsoleTools {

    private static final char ELLIPSIS = '…';

    private static final Terminal terminal;

    static {
        terminal = Invocation.runUnchecked(() ->
                TerminalBuilder.builder().color(true).encoding(StandardCharsets.UTF_8).build());
    }

    private ConsoleTools() {
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
        return text + ELLIPSIS;
    }

    public static int getLineLength() {
        return Math.max(60, terminal.getWidth());
    }
}
