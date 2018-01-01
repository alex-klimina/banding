package banding.generator;

import banding.entity.Interval;
import banding.entity.Track;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static banding.generator.RandomTrackGenerator.generateRandomInterval;
import static banding.generator.RandomTrackGenerator.generateRandomTrackLike;
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

    @Test
    public void shouldGenerateTrackWithPreassignedLengthByAnotherTrack() {
        Track queryTrack = new Track();
        queryTrack.addInterval(new Interval(5, 20));
        queryTrack.addInterval(new Interval(27, 42));
        queryTrack.addInterval(new Interval(47, 62));
        queryTrack.addInterval(new Interval(197, 212));
        queryTrack.addInterval(new Interval(219, 234));
        queryTrack.addInterval(new Interval(242, 257));

        Track referenceTrack = new Track();
        referenceTrack.addInterval(new Interval(0, 300));

        Track generatedTrack = generateRandomTrackLike(referenceTrack.getLength(), queryTrack);

        assertThat(generatedTrack.getTrackEnd(), lessThanOrEqualTo(referenceTrack.getLength()));
        assertThat(generatedTrack.getNumberOfIntervals(), is(queryTrack.getNumberOfIntervals()));
        Set<Long> generatedTrackIntervalSet = generatedTrack.getIntervals().stream()
                .map(Interval::getLength)
                .collect(Collectors.toSet());
        Set<Long> queryTrackIntervalSet = queryTrack.getIntervals().stream()
                .map(Interval::getLength)
                .collect(Collectors.toSet());
        assertThat(generatedTrackIntervalSet, is(queryTrackIntervalSet));
    }

    @Test
    public void shouldPrintGeneratedTrack() {
        Track queryTrack = new Track();
        queryTrack.addInterval(new Interval(5, 20));
        queryTrack.addInterval(new Interval(27, 42));
        queryTrack.addInterval(new Interval(47, 62));
        queryTrack.addInterval(new Interval(197, 212));
        queryTrack.addInterval(new Interval(219, 234));
        queryTrack.addInterval(new Interval(242, 257));

        Track referenceTrack = new Track();
        referenceTrack.addInterval(new Interval(0, 300));

        Track generatedTrack = generateRandomTrackLike(referenceTrack.getLength(), queryTrack);

        System.out.println(generatedTrack);

        generatedTrack.sortIntervals();
        System.out.println(generatedTrack);

    }

    @Test (expected = RuntimeException.class)
    public void shouldThrowRuntimeExceptionIfIntervalDoesNotFitInRange() {
        Interval randomInterval = generateRandomInterval(15, 0, 10);
    }

    @Test
    public void shouldCreateRandomIntervalWithLengthExactEqualsRange() {
        Interval interval = generateRandomInterval(11, 0, 10);
        assertThat(interval.getLength(), is(11L));
    }


}