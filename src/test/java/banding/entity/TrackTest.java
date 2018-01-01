package banding.entity;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static banding.entity.Track.areIntervalAndTrackIntersect;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

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
}