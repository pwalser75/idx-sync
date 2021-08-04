package ch.frostnova.cli.idx.sync.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test for {@link LimitedList}.
 */
class LimitedListTest {


    @Test
    void testZeroListSize() {
        List<Integer> list = new LimitedList<>(0);
        assertThat(list).isEmpty();

        list.add(1);
        assertThat(list).isEmpty();

        list.addAll(asList(2, 3, 4));
        assertThat(list).isEmpty();
    }

    @Test
    void testLimitedListSize() {
        List<Integer> list = new LimitedList<>(5);

        list.add(1);
        list.add(2);
        list.add(3);
        assertThat(list).containsExactly(1, 2, 3);
        list.add(4);
        list.add(5);
        assertThat(list).containsExactly(1, 2, 3, 4, 5);
        list.add(6);
        list.add(7);
        assertThat(list).containsExactly(3, 4, 5, 6, 7);

        list.addAll(asList(8, 9, 10));
        assertThat(list).containsExactly(6, 7, 8, 9, 10);
        list.addAll(asList(11, 12, 13, 14, 15, 16, 17));
        assertThat(list).containsExactly(13, 14, 15, 16, 17);
    }

    @Test
    void testNegativeListSize() {
        assertThatThrownBy(() -> new LimitedList<>(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
