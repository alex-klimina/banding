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

    @Test
    public void shouldCreateIntervalWithName() {
        Interval interval = new Interval("SampleInterval");
        assertThat(interval.getName(), is("SampleInterval"));
        assertThat(interval.getStartIndex(), is(0L));
        assertThat(interval.getEndIndex(), is(0L));
    }

    @Test
    public void shouldComputeIntervalsUnion() {
        Interval interval1 = new Interval(0, 5);
        Interval interval2 = new Interval(3, 10);
        Interval expectedUnionInterval = new Interval(0, 10);
        assertThat(Interval.intervalsUnion(interval1, interval2), is(expectedUnionInterval));
    }

    @Test (expected = RuntimeException.class)
    public void shouldThrowExceptionWhenIntervalsAreNotIntersected() {
        Interval interval1 = new Interval(0, 5);
        Interval interval2 = new Interval(7, 10);
        Interval.intervalsUnion(interval1, interval2);
    }
}