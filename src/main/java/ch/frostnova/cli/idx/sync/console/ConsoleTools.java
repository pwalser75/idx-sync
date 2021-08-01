package ch.frostnova.cli.idx.sync.console;

import static ch.frostnova.cli.idx.sync.console.AnsiEscape.*;

public final class ConsoleTools {

    private static final char ELLIPSIS = '…';

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
}
