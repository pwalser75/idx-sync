package ch.frostnova.cli.idx.sync.console;

import static ch.frostnova.cli.idx.sync.console.Console.isModern;

public enum TextIcon {

    ROCKET("\uD83D\uDE80", ""),
    ELLIPSIS("\u2026", "..."),
    SYNC("\uD83D\uDD04", "<>"),
    CHECK("\u2705", "*"),
    ERROR("\u274C", "[x]");

    private final String unicode;
    private final String plain;

    TextIcon(String unicode, String plain) {
        this.unicode = unicode;
        this.plain = plain;
    }

    public String getUnicode() {
        return unicode;
    }

    public String getPlain() {
        return plain;
    }

    @Override
    public String toString() {
        return isModern() ? unicode : plain;
    }
}
