package ch.frostnova.cli.idx.sync.util;

import org.junit.jupiter.api.Test;

import static ch.frostnova.cli.idx.sync.util.ByteFormat.formatBytes;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ByteFormat}.
 */
public class ByteFormatTest {

    @Test
    public void testFormat() {

        assertThat(formatBytes(0)).isEqualTo("0 bytes");

        assertThat(formatBytes(1)).isEqualTo("1 bytes");
        assertThat(formatBytes(12)).isEqualTo("12 bytes");
        assertThat(formatBytes(123)).isEqualTo("123 bytes");

        assertThat(formatBytes(1234)).isEqualTo("1.2 kB");
        assertThat(formatBytes(12345)).isEqualTo("12.3 kB");
        assertThat(formatBytes(123456)).isEqualTo("123.5 kB");

        assertThat(formatBytes(1234567)).isEqualTo("1.2 MB");
        assertThat(formatBytes(12345678)).isEqualTo("12.3 MB");
        assertThat(formatBytes(123456789)).isEqualTo("123.5 MB");

        assertThat(formatBytes(1234567890)).isEqualTo("1.2 GB");
        assertThat(formatBytes(12345678901L)).isEqualTo("12.3 GB");
        assertThat(formatBytes(123456789012L)).isEqualTo("123.5 GB");

        assertThat(formatBytes(1234567890123L)).isEqualTo("1.2 TB");
        assertThat(formatBytes(12345678901234L)).isEqualTo("12.3 TB");
        assertThat(formatBytes(123456789012345L)).isEqualTo("123.5 TB");

        assertThat(formatBytes(1234567890123456L)).isEqualTo("1.2 PB");
        assertThat(formatBytes(12345678901234567L)).isEqualTo("12.3 PB");
        assertThat(formatBytes(123456789012345678L)).isEqualTo("123.5 PB");

        assertThat(formatBytes(1234567890123456789L)).isEqualTo("1234.6 PB");
        assertThat(formatBytes(5555555555555555555L)).isEqualTo("5555.6 PB");

        assertThat(formatBytes(-1)).isEqualTo("-1 bytes");
        assertThat(formatBytes(-12)).isEqualTo("-12 bytes");
        assertThat(formatBytes(-123)).isEqualTo("-123 bytes");

        assertThat(formatBytes(-1234)).isEqualTo("-1.2 kB");
        assertThat(formatBytes(-12345)).isEqualTo("-12.3 kB");
        assertThat(formatBytes(-123456)).isEqualTo("-123.5 kB");

        assertThat(formatBytes(-1234567)).isEqualTo("-1.2 MB");
        assertThat(formatBytes(-12345678)).isEqualTo("-12.3 MB");
        assertThat(formatBytes(-123456789)).isEqualTo("-123.5 MB");

        assertThat(formatBytes(-1234567890)).isEqualTo("-1.2 GB");
        assertThat(formatBytes(-12345678901L)).isEqualTo("-12.3 GB");
        assertThat(formatBytes(-123456789012L)).isEqualTo("-123.5 GB");

        assertThat(formatBytes(-1234567890123L)).isEqualTo("-1.2 TB");
        assertThat(formatBytes(-12345678901234L)).isEqualTo("-12.3 TB");
        assertThat(formatBytes(-123456789012345L)).isEqualTo("-123.5 TB");

        assertThat(formatBytes(-1234567890123456L)).isEqualTo("-1.2 PB");
        assertThat(formatBytes(-12345678901234567L)).isEqualTo("-12.3 PB");
        assertThat(formatBytes(-123456789012345678L)).isEqualTo("-123.5 PB");

        assertThat(formatBytes(-1234567890123456789L)).isEqualTo("-1234.6 PB");
        assertThat(formatBytes(-5555555555555555555L)).isEqualTo("-5555.6 PB");

    }
}
