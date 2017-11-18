package banding.generator;

import banding.entity.Interval;
import banding.entity.Track;
import org.junit.Test;

import java.util.Collections;

import static banding.generator.RandomTrackGenerator.generateRandomInterval;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RandomTrackGeneratorTest {

    @Test
    public void shouldGenerateRandomIntervalByAnotherInterval() {
        Interval originInterval = new Interval(4, 9);
        Interval randomInterval = generateRandomInterval(originInterval, 0, 10);
        assertThat(randomInterval.getLength(), is(originInterval.getLength()));
    }

    @Test
    public void shouldGenerateRandomIntervalWithPreassignedLength() {
        Interval originInterval = new Interval(4, 9);
        Interval randomInterval = generateRandomInterval(originInterval.getLength(), 0, 10);
        assertThat(randomInterval.getLength(), is(originInterval.getLength()));
    }

    @Test
    public void shouldGenerateIntervalInTrack() {
        Interval originInterval = new Interval(4, 9);
        Track originTrack = new Track(Collections.singletonList(new Interval(0, 15)));
        Interval randomInterval = generateRandomInterval(originInterval, originTrack);

        assertThat(randomInterval.getLength(), is(originInterval.getLength()));
        assertThat(randomInterval.getStartIndex(), greaterThanOrEqualTo(originTrack.getTrackStart()));
        assertThat(randomInterval.getEndIndex(), lessThanOrEqualTo(originTrack.getTrackEnd()));
    }


}