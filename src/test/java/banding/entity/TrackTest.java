package banding.entity;

import org.junit.Test;

import static banding.entity.Track.areIntervalAndTrackIntersect;
import static org.hamcrest.core.Is.is;
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
}