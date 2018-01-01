package banding.generator;

import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.entity.Track;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static banding.generator.RandomTrackGenerator.generateChromosomeSetByReferenceLike;
import static banding.generator.RandomTrackGenerator.generateGenomeByReferenceLike;
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

    Track referenceTrack1;
    Track referenceTrack2;
    Chromosome referenceChr1;
    Chromosome referenceChr2;
    List<Chromosome> chromosomesReference;
    Genome genomeReference;

    Track queryTrack1;
    Track queryTrack2;
    Chromosome queryChr1;
    Chromosome queryChr2;
    List<Chromosome> chromosomesQuery;
    Genome genomeQuery;

    @Before
    public void initialize() {
        referenceTrack1 = new Track();
        referenceTrack1.addInterval(0, 3)
                .addInterval(5, 8)
                .addInterval(10, 15);
        referenceChr1 = new Chromosome("chr1", referenceTrack1, 0, 20);

        referenceTrack2 = new Track();
        referenceTrack2.addInterval(0, 3)
                .addInterval(5, 8)
                .addInterval(10, 15);
        referenceChr2 = new Chromosome("chr2", referenceTrack2, 0, 30);

        chromosomesReference = new ArrayList<>();
        chromosomesReference.add(referenceChr1);
        chromosomesReference.add(referenceChr2);

        genomeReference = new Genome();
        genomeReference.addChromosome(referenceChr1);
        genomeReference.addChromosome(referenceChr2);

        queryTrack1 = new Track();
        queryTrack1.addInterval(4,7);
        queryChr1 = new Chromosome("chr1", queryTrack1, 0, 20);
        queryTrack2 = new Track();
        queryTrack2.addInterval(8, 10);
        queryChr2 = new Chromosome("chr2", queryTrack2, 0, 30);

        chromosomesQuery = new ArrayList<>();
        chromosomesQuery.add(queryChr1);
        chromosomesQuery.add(queryChr2);
        genomeQuery = new Genome(chromosomesQuery);
    }

    @Test
    public void shouldGenerateRandomGenomeByReference() {
        Genome generatedGenome = generateGenomeByReferenceLike(genomeReference, genomeQuery);
        assertThat(generatedGenome.getNumberOfIntervals(), is(genomeQuery.getNumberOfIntervals()));
        assertThat(generatedGenome.getCoverage(), is(genomeQuery.getCoverage()));
        assertThat(generatedGenome.getLength(), is(genomeReference.getLength()));
    }


    @Test
    public void shouldGenerateChromosomeSetByReference() {
        Map<String, Track> reference = new HashMap<>();
        genomeReference.getChromosomes()
                .forEach(x -> reference.put(x.getName(), x.getTrack()));
        Map<String, Track> query = new HashMap<>();
        genomeQuery.getChromosomes()
                .forEach(x -> query.put(x.getName(), x.getTrack()));
        Map<String, Track> generatedTrackMap = generateChromosomeSetByReferenceLike(reference, query);
        assertThat(generatedTrackMap.get("chr1").getNumberOfIntervals(), is(genomeQuery.getChromosome("chr1").getNumberOfIntervals()));
        assertThat(generatedTrackMap.get("chr1").getCoverage(), is(genomeQuery.getChromosome("chr1").getCoverage()));
    }

}