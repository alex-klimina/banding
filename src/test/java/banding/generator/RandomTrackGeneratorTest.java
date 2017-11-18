package banding.generator;

import banding.entity.Interval;
import org.junit.Test;

import static banding.generator.RandomTrackGenerator.generateRandomInterval;
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
    }


}