package banding.entity;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class IntervalTest {

    @Test
    public void shouldReturnWhetherIntervalIntersectedOrNot() {
        Interval interval1 = new Interval("", 0, 5);
        Interval interval2 = new Interval("", 6, 10);
        assertThat(Interval.areIntervalsIntersected(interval1, interval2), is(false));

        interval1 = new Interval("", 0, 8);
        interval2 = new Interval("", 5, 10);
        assertThat(Interval.areIntervalsIntersected(interval1, interval2), is(true));

    }

    @Test
    public void shouldComputeMiddleOfInterval() {
        assertThat(Interval.middleOfInterval(new Interval(1, 5)), is(3L));
        assertThat(Interval.middleOfInterval(new Interval(1, 1)), is(1L));
        assertThat(Interval.middleOfInterval(new Interval(1, 6)), is(3L));
    }
}