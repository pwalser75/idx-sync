package ch.frostnova.cli.idx.sync.console;

import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * ANSI escape sequences.
 */
public enum AnsiEscape {

    ANSI_RESET("0m"),
    ANSI_BOLD("1m"),
    ANSI_GRAY("38;5;36m"),

    ANSI_BLUE("38;5;75m"),
    ANSI_CYAN("38;5;79m"),
    ANSI_GREEN("38;5;70m"),
    ANSI_YELLOW("38;5;178m"),
    ANSI_ORANGE("38;5;208m"),
    ANSI_RED("38;5;196m"),

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
    public static String format(Object text, AnsiEscape... style) {
        return stream(style).map(AnsiEscape::getEscapeSequence).collect(joining()) + Optional.ofNullable(text).orElse("") + ANSI_RESET;
    }

    @Override
    public String toString() {
        return escapeSequence;
    }
}