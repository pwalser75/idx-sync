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

        assertThat(new PathFilter("*/$Recycle.Bin").test(Path.of("C:/$Recycle.Bin"))).isTrue();
        assertThat(new PathFilter("**/node_modules/**/*.js").test(Path.of("/media/colin/workspace/demo-game/node_modules/phaser/src/renderer/webgl/shaders/FXDisplacement-frag.js"))).isTrue();
        assertThat(new PathFilter("**/node_modules/**").test(Path.of("/media/colin/workspace/demo-game/node_modules/phaser/src/renderer/webgl/shaders/FXDisplacement-frag.js"))).isTrue();
        assertThat(new PathFilter("**/node_modules/**/.js").test(Path.of("/media/colin/workspace/demo-game/node_modules/phaser/src/renderer/webgl/shaders/FXDisplacement-frag.js"))).isFalse();
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
        assertThat(new PathFilter("**/test.txt").test(Path.of("test.txt"))).isFalse();
        assertThat(new PathFilter("**/test.txt").test(Path.of("foo/test.txt"))).isTrue();
        assertThat(new PathFilter("**/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();

        assertThat(new PathFilter("foo/**/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();
        assertThat(new PathFilter("bla/**/test.txt").test(Path.of("foo/bla/test.txt"))).isFalse();

        assertThat(new PathFilter("**/**/test.txt").test(Path.of("foo/bla/test.txt"))).isTrue();
    }

    @Test
    void testDefaultExcludes() {
        var filter = PathFilter.defaultExcludes();

        assertThat(filter.test(Path.of(""))).isFalse();

        assertThat(filter.test(Path.of("/"))).isFalse();
        assertThat(filter.test(Path.of("/$Recycle.Bin"))).isTrue();
        assertThat(filter.test(Path.of("/System Volume Information"))).isTrue();

        assertThat(filter.test(Path.of("/foo/"))).isFalse();
        assertThat(filter.test(Path.of("/foo/"))).isFalse();
        assertThat(filter.test(Path.of("/foo/$Recycle.Bin"))).isFalse();
        assertThat(filter.test(Path.of("/foo/$Recycle.Bin/"))).isFalse();
        assertThat(filter.test(Path.of("/foo/System Volume Information"))).isFalse();

        assertThat(filter.test(Path.of("C:\\"))).isFalse();
        assertThat(filter.test(Path.of("C:\\Windows"))).isTrue();
        assertThat(filter.test(Path.of("C:\\$Recycle.Bin"))).isTrue();
        assertThat(filter.test(Path.of("C:\\System Volume Information"))).isTrue();

        assertThat(filter.test(Path.of("C:/"))).isFalse();
        assertThat(filter.test(Path.of("C:/Windows"))).isTrue();
        assertThat(filter.test(Path.of("C:/$Recycle.Bin"))).isTrue();
        assertThat(filter.test(Path.of("C:/System Volume Information"))).isTrue();

        assertThat(filter.test(Path.of("C:\\foo"))).isFalse();
        assertThat(filter.test(Path.of("C:\\foo\\"))).isFalse();
        assertThat(filter.test(Path.of("C:\\foo\\$Recycle.Bin"))).isFalse();
        assertThat(filter.test(Path.of("C:\\foo\\System Volume Information"))).isFalse();
    }
}
