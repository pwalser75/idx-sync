package ch.frostnova.cli.idx.sync.filter;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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

    private final static String ANY_CHAR_SEQUENCE = "[^/]*";
    private final static String ANY_CHAR = "[^/]";
    private final static String ANY_PATH_SEQUENCE = "(?:[^/]+(?:/[^/]+)*/)?";

    private final static String ESCAPE_CHARS = ".\\?*+:[]()";

    private final Pattern pattern;

    public PathFilter(String antWildcardPattern) {
        String placeholderAnyPathSequence = randomUUID().toString();
        String placeholderAnyCharSequence = randomUUID().toString();
        String placeholderAnyChar = randomUUID().toString();

        String regex = escape(antWildcardPattern
                .replace("**/", placeholderAnyPathSequence)
                .replace("*", placeholderAnyCharSequence)
                .replace("?", placeholderAnyChar))
                .replace(placeholderAnyPathSequence, ANY_PATH_SEQUENCE)
                .replace(placeholderAnyCharSequence, ANY_CHAR_SEQUENCE)
                .replace(placeholderAnyChar, ANY_CHAR);
        pattern = Pattern.compile(regex);
    }

    private static String escape(String text) {
        StringBuffer buffer = new StringBuffer();
        for (char c : text.toCharArray()) {
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
