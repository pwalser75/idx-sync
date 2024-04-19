package ch.frostnova.cli.idx.sync.filter;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;

/**
 * A filter matching {@link Path}s by ant-style wildcard patterns:
 * <ul>
 *     <li><code>*</code> matches any character sequence (0..n)</li>
 *     <li><code>?</code> matches any character (1)</li>
 *     <li><code>**</code><code>/</code> matches any path sequence</li>
 * </ul>
 */
public class PathFilter implements Predicate<Path> {

    public final static Predicate<Path> NONE = path -> false;

    private final static String ANY_CHAR_SEQUENCE = "[^\\/]*";
    private final static String ANY_CHAR = "[^\\/]";
    private final static String ANY_PATH_SEQUENCE = ".*";

    private final static String ESCAPE_CHARS = ".\\?$*+-:[]()";

    private final static String[] DEFAULT_EXCLUDES = {
            ".idxsync",
            "**/.idxsync",
            "*/$RECYCLE.BIN",
            "*/$Recycle.Bin",
            "*/.Trash-*",
            "*/System Volume Information",
            "*/Windows",
            "**/Thumbs.db",
            ".idea",
            ".gradle",
            "**/node_modules/**"
    };

    private final Pattern pattern;

    public PathFilter(String antWildcardPattern) {
        var placeholderAnyPathSequence = placeholder();
        var placeholderAnyCharSequence = placeholder();
        var placeholderAnyChar = placeholder();

        var regex = escape(antWildcardPattern
                .replace("**", placeholderAnyPathSequence)
                .replace("*", placeholderAnyCharSequence)
                .replace("?", placeholderAnyChar))
                .replace(placeholderAnyPathSequence, ANY_PATH_SEQUENCE)
                .replace(placeholderAnyCharSequence, ANY_CHAR_SEQUENCE)
                .replace(placeholderAnyChar, ANY_CHAR);
        pattern = Pattern.compile("^" + regex + "$");
    }

    private static String placeholder() {
        return randomUUID().toString().replace("-", "");
    }

    public static Predicate<Path> defaultExcludes() {
        return anyOf(DEFAULT_EXCLUDES);
    }

    public static Predicate<Path> anyOf(String... antWildcardPatterns) {
        return anyOf(Arrays.stream(antWildcardPatterns));
    }

    public static Predicate<Path> anyOf(Collection<String> antWildcardPatterns) {
        return anyOf(antWildcardPatterns.stream());
    }

    private static Predicate<Path> anyOf(Stream<String> antWildcardPatterns) {
        return antWildcardPatterns
                .map(PathFilter::new)
                .collect(Collectors.reducing(NONE, (a, b) -> a.or(b)));
    }

    private static String escape(String text) {
        var buffer = new StringBuffer();
        for (var c : text.toCharArray()) {
            if (ESCAPE_CHARS.contains(c + "")) {
                buffer.append("\\");
            }
            buffer.append(c);
        }
        return buffer.toString();
    }

    @Override
    public boolean test(Path path) {
        return pattern.matcher(path.toString().replace('\\', '/')).matches();
    }
}
