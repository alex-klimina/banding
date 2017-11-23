package banding.metric;

import banding.IntervalReader;
import banding.entity.Interval;
import banding.entity.Track;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class JaccardTestTest {

    @Test
    public void shouldComputeIntersectionOfIntervals() {
        Deque<Interval> reference = new ArrayDeque<>();
        reference.add(new Interval(5, 55));
        reference.add(new Interval(204, 255));

        Deque<Interval> query = new ArrayDeque<>();
        query.add(new Interval(5, 20));
        query.add(new Interval(27, 42));
        query.add(new Interval(47, 62));
        query.add(new Interval(197, 212));
        query.add(new Interval(219, 234));
        query.add(new Interval(242, 257));

        assertThat(JaccardTest.getIntersectionValue(reference, query), is(74));
    }

    @Test
    public void printUnionAndIntersection() {
        Deque<Interval> reference = new ArrayDeque<>();
        reference.add(new Interval(5, 55));
        reference.add(new Interval(204, 255));

        Deque<Interval> query = new ArrayDeque<>();
        query.add(new Interval(5, 20));
        query.add(new Interval(27, 42));
        query.add(new Interval(47, 62));
        query.add(new Interval(197, 212));
        query.add(new Interval(219, 234));
        query.add(new Interval(242, 257));

        System.out.println("TrackUnion");
        Track.tracksUnion(query, reference)
                .forEach(x -> System.out.println(x.getStartIndex() + "\t" + x.getEndIndex() + "\t" + x.getLength()));

        reference = new ArrayDeque<>();
        reference.add(new Interval(5, 55));
        reference.add(new Interval(204, 255));

        query = new ArrayDeque<>();
        query.add(new Interval(5, 20));
        query.add(new Interval(27, 42));
        query.add(new Interval(47, 62));
        query.add(new Interval(197, 212));
        query.add(new Interval(219, 234));
        query.add(new Interval(242, 257));

        System.out.println("TrackIntersection");
        Track.trackIntersection(query, reference)
                .forEach(x -> System.out.println(x.getStartIndex() + "\t" + x.getEndIndex() + "\t" + x.getLength()));
    }

    @Test
    public void shouldComputeUnionOfIntervals() {
        Deque<Interval> reference = new ArrayDeque<>();
        reference.add(new Interval(5, 55));
        reference.add(new Interval(204, 255));

        Deque<Interval> query = new ArrayDeque<>();
        query.add(new Interval(5, 20));
        query.add(new Interval(27, 42));
        query.add(new Interval(47, 62));
        query.add(new Interval(197, 212));
        query.add(new Interval(219, 234));
        query.add(new Interval(242, 257));

        Deque<Interval> expectedTrack = new ArrayDeque<>();
        expectedTrack.add(new Interval(5, 62));
        expectedTrack.add(new Interval(197, 257));

        assertArrayEquals(Track.tracksUnion(reference, query).toArray(), expectedTrack.toArray());

    }

    @Test
    public void shouldJaccardTestReturn0643() throws IOException {
        String reference = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/query.txt";

        Deque<Interval> referenceIntervals = new IntervalReader(reference).read();
        Deque<Interval> queryIntervals = new IntervalReader(query).read();

        assertThat(JaccardTest.computeJaccardStatisticForChromosome(queryIntervals, referenceIntervals), is(closeTo(0.632, 0.001)));
    }

    @Test
    public void shouldReturnWhetherIntervalIntersectedOrNot() {
        Interval interval1 = new Interval("", 0, 5);
        Interval interval2 = new Interval("", 6, 10);
        assertThat(Interval.areIntervalsIntersected(interval1, interval2), is(false));

        interval1 = new Interval("", 0, 8);
        interval2 = new Interval("", 5, 10);
        assertThat(Interval.areIntervalsIntersected(interval1, interval2), is(true));

    }
}