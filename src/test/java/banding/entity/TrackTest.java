package banding.entity;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import static banding.entity.Track.areIntervalAndTrackIntersect;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TrackTest {

    @Test
    public void checkIntervalAndTrackIntersect() {
        Track track = new Track();
        track.addInterval(0, 15)
                .addInterval(30,45);

        assertThat(areIntervalAndTrackIntersect(new Interval(3, 8), track), is(true));
        assertThat(areIntervalAndTrackIntersect(new Interval(3, 31), track), is(true));

        assertThat(areIntervalAndTrackIntersect(new Interval(16, 25), track), is(false));

    }

    @Test
    public void checkCreatingTrackThroughDequeAndListContainsEqualArrayInterval() {
        Interval interval1 = new Interval(0, 4);
        Interval interval2 = new Interval(6, 12);
        Interval interval3 = new Interval(14, 40);

        Deque<Interval> intervalDeque = new ArrayDeque<>();
        intervalDeque.add(interval1);
        intervalDeque.add(interval2);
        intervalDeque.add(interval3);

        List<Interval> intervalList = new ArrayList<>();
        intervalList.add(interval1);
        intervalList.add(interval2);
        intervalList.add(interval3);

        Track trackCreatedByDeque = new Track(intervalDeque);
        Track trackCreatedByList = new Track(intervalList);
        assertArrayEquals(trackCreatedByDeque.getIntervals().toArray(), trackCreatedByList.getIntervals().toArray());
    }

    @Test
    public void checkStartOfTrackOnSortedTrack() {
        Interval interval1 = new Interval(0, 4);
        Interval interval2 = new Interval(6, 12);
        Interval interval3 = new Interval(14, 40);

        Track track = new Track();
        track.addInterval(interval1)
                .addInterval(interval2)
                .addInterval(interval3);

        assertThat(track.getTrackStart(), is(0L));
    }

    @Test
    public void checkEndOfTrackOnSortedTrack() {
        Interval interval1 = new Interval(0, 4);
        Interval interval2 = new Interval(6, 12);
        Interval interval3 = new Interval(14, 40);

        Track track = new Track();
        track.addInterval(interval1)
                .addInterval(interval2)
                .addInterval(interval3);

        assertThat(track.getTrackEnd(), is(40L));
    }

    @Test
    public void checkStartOfTrackOnUnsortedTrack() {
        Interval interval1 = new Interval(50, 54);
        Interval interval2 = new Interval(6, 12);
        Interval interval3 = new Interval(18, 40);

        Track track = new Track();
        track.addInterval(interval1)
                .addInterval(interval2)
                .addInterval(interval3);

        assertThat(track.getTrackStart(), is(6L));
    }

    @Test
    public void checkEndOfTrackOnUnsortedTrack() {
        Interval interval1 = new Interval(50, 54);
        Interval interval2 = new Interval(6, 12);
        Interval interval3 = new Interval(18, 40);

        Track track = new Track();
        track.addInterval(interval1)
                .addInterval(interval2)
                .addInterval(interval3);

        assertThat(track.getTrackEnd(), is(54L));
    }

    @Test
    public void checkThatTrackContainsPoint() {
        Interval interval1 = new Interval(50, 54);
        Interval interval2 = new Interval(6, 12);
        Interval interval3 = new Interval(18, 40);

        Track track = new Track();
        track.addInterval(interval1)
                .addInterval(interval2)
                .addInterval(interval3);

        assertTrue(track.containsPoint(6)); // start
        assertTrue(track.containsPoint(20)); // middle
        assertTrue(track.containsPoint(54)); // end
    }

    @Test
    public void checkThatTrackDoesNotContainPoint() {
        Interval interval1 = new Interval(50, 54);
        Interval interval2 = new Interval(6, 12);
        Interval interval3 = new Interval(18, 40);

        Track track = new Track();
        track.addInterval(interval1)
                .addInterval(interval2)
                .addInterval(interval3);

        assertFalse(track.containsPoint(5)); // before start
        assertFalse(track.containsPoint(45)); // not close to any of intervals
        assertFalse(track.containsPoint(55)); // after end
    }

    @Test
    public void shouldComputeUnionOfIntervals() {
        Deque<Interval> reference = new ArrayDeque<>();
        reference.add(new Interval(5, 55));
        reference.add(new Interval(204, 255));

        Deque<Interval> query = new ArrayDeque<>();
        query.add(new Interval(6, 20));
        query.add(new Interval(27, 42));
        query.add(new Interval(47, 62));
        query.add(new Interval(197, 212));
        query.add(new Interval(219, 234));
        query.add(new Interval(242, 257));

        Deque<Interval> expectedTrack = new ArrayDeque<>();
        expectedTrack.add(new Interval(5, 62));
        expectedTrack.add(new Interval(197, 257));

        assertArrayEquals(expectedTrack.toArray(), Track.tracksUnion(reference, query).toArray());
    }

    @Test
    public void unionAndIntersectionForIdenticalTrackShouldBeEqual() {
        Deque<Interval> track = new ArrayDeque<>();
        track.add(new Interval(5, 20));
        track.add(new Interval(27, 42));
        track.add(new Interval(47, 62));
        track.add(new Interval(197, 212));
        track.add(new Interval(219, 234));
        track.add(new Interval(242, 257));

        Deque<Interval> intersection = Track.trackIntersection(track, track).collect(Collectors.toCollection(ArrayDeque::new));
        Deque<Interval> union = Track.tracksUnion(track, track);
        assertArrayEquals(intersection.toArray(), union.toArray());
    }

    @Test
    public void checkLengthOnSortedTrack() {
        Interval interval1 = new Interval(0, 5);
        Interval interval2 = new Interval(8, 12);
        Interval interval3 = new Interval(18, 40);

        Track track = new Track();
        track.addInterval(interval1)
                .addInterval(interval2)
                .addInterval(interval3);

        assertThat(track.getLength(), is(41L));
    }

    @Test
    public void checkLengthOnUnsortedTrack() {
        Interval interval1 = new Interval(50, 54);
        Interval interval2 = new Interval(6, 12);
        Interval interval3 = new Interval(18, 40);

        Track track = new Track();
        track.addInterval(interval1)
                .addInterval(interval2)
                .addInterval(interval3);

        assertThat(track.getLength(), is(49L));
    }

    @Test
    public void shouldCheckThatNumberOfIntervalIsCorrect() {
        Track track = new Track();
        track.addInterval(new Interval(5, 20));
        track.addInterval(new Interval(27, 42));
        track.addInterval(new Interval(47, 62));
        track.addInterval(new Interval(197, 212));
        track.addInterval(new Interval(219, 234));
        track.addInterval(new Interval(242, 257));

        Interval interval1 = new Interval(50, 54);
        Interval interval2 = new Interval(6, 12);
        Interval interval3 = new Interval(18, 40);

        Deque<Interval> intervalDeque = new ArrayDeque<>();
        intervalDeque.add(interval1);
        intervalDeque.add(interval2);
        intervalDeque.add(interval3);

        List<Interval> intervalList = new ArrayList<>();
        intervalList.add(interval1);
        intervalList.add(interval2);
        intervalList.add(interval3);

        Track trackCreatedByDeque = new Track(intervalDeque);
        Track trackCreatedByList = new Track(intervalList);

        assertThat(trackCreatedByDeque.getNumberOfIntervals(), is(3));
        assertThat(trackCreatedByList.getNumberOfIntervals(), is(3));
        assertThat(track.getNumberOfIntervals(), is(6));
    }
}