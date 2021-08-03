package ch.frostnova.cli.idx.sync.filter;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PathFilter}.
 */
class PathFilterTest {

    @Test
    void testLiteral() {

        assertThat(new PathFilter("test.txt").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("test.txt").test(Path.of("test_txt"))).isFalse();

        assertThat(new PathFilter("foo/bla/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();
    }

    @Test
    void testWildcardAsterisk() {
        assertThat(new PathFilter("*.txt").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("*.txt").test(Path.of("test_txt"))).isFalse();

        assertThat(new PathFilter("te*.txt").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("t*t.txt").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("test*.txt").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("*test.txt").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("*t*e*s*t*.txt").test(Path.of("test.txt"))).isTrue();

        assertThat(new PathFilter("foo/bla/*.txt").test(Path.of("foo/bla/test.txt"))).isTrue();
        assertThat(new PathFilter("*/bla/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();
        assertThat(new PathFilter("foo/*/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();
        assertThat(new PathFilter("*/*/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();

        assertThat(new PathFilter("*/test.txt").test(Path.of("foo/bla/test.txt"))).isFalse();
    }

    @Test
    void testWildcardQuestionMark() {
        assertThat(new PathFilter("?est.txt").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("?est.txt").test(Path.of("test_txt"))).isFalse();

        assertThat(new PathFilter("te?t.txt").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("t??t.txt").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("test.t??").test(Path.of("test.txt"))).isTrue();

        assertThat(new PathFilter("???.???").test(Path.of("test.txt"))).isFalse();
        assertThat(new PathFilter("????.???").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("?????.???").test(Path.of("test.txt"))).isFalse();

        assertThat(new PathFilter("foo/bla/te?t.txt").test(Path.of("foo/bla/test.txt"))).isTrue();
        assertThat(new PathFilter("?oo/bla/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();
        assertThat(new PathFilter("foo/b?a/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();

        assertThat(new PathFilter("foo?bla/test.txt").test(Path.of("foo/bla/test.txt"))).isFalse();
    }

    @Test
    void testWildcardAntPath() {
        assertThat(new PathFilter("**/test.txt").test(Path.of("test.txt"))).isTrue();
        assertThat(new PathFilter("**/test.txt").test(Path.of("foo/test.txt"))).isTrue();
        assertThat(new PathFilter("**/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();

        assertThat(new PathFilter("foo/**/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();
        assertThat(new PathFilter("bla/**/test.txt").test(Path.of("foo/bla/test.txt"))).isFalse();

        assertThat(new PathFilter("**/**/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();
    }
}
