package ch.frostnova.cli.idx.sync.console;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * ANSI escape sequences.
 */
public enum AnsiEscape {

    ANSI_RESET("0m"),
    ANSI_BOLD("1m"),
    ANSI_GRAY("38;5;36m"),
    ANSI_YELLOW("38;5;178m"),
    ANSI_GREEN("38;5;70m"),
    ANSI_BLUE("38;5;75m"),
    ANSI_CYAN("38;5;79m"),
    CURSOR_START_LINE("1000D"),
    CLEAR_FROM_CURSOR("0K");

    private String escapeSequence;

    AnsiEscape(String code) {
        escapeSequence = "\u001b[" + code;
    }

    private String getEscapeSequence() {
        return escapeSequence;
    }

    /**
     * Format a text using the given styles
     *
     * @param text  text
     * @param style styles to apply
     * @return ansi-formatted text
     */
    public static String format(String text, AnsiEscape... style) {
        return stream(style).map(AnsiEscape::getEscapeSequence).collect(joining()) + text + ANSI_RESET;
    }

    @Override
    public String toString() {
        return escapeSequence;
    }
}