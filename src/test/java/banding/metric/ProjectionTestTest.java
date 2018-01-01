package banding.metric;

import banding.IntervalReader;
import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.entity.Track;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static banding.metric.ProjectionTest.countProjection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ProjectionTestTest {

    @Test
    public void shouldProjectionTestReturn5() throws IOException {
        String reference = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/query.txt";

        Queue<Interval> referenceIntervals = new IntervalReader(reference).read();
        Queue<Interval> queryIntervals = new IntervalReader(query).read();

        assertThat(countProjection(referenceIntervals, queryIntervals), is(6L));
    }

    Genome genomeReference;
    Genome genomeQuery;

    @Before
    public void initialize() throws IOException {
        String reference = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/query.txt";

        Deque<Interval> referenceIntervals = new IntervalReader(reference).read();
        Deque<Interval> queryIntervals = new IntervalReader(query).read();
        genomeReference = new Genome();
        long startIndexReference = referenceIntervals.getFirst().getStartIndex();
        long endIndexReference = referenceIntervals.getLast().getEndIndex();
        genomeReference.addChromosome(new Chromosome("chr1",
                new Track(referenceIntervals),
                startIndexReference,
                endIndexReference));

        genomeQuery = new Genome();
        long startIndexQuery = queryIntervals.getFirst().getStartIndex();
        long endIndexQuery = queryIntervals.getLast().getEndIndex();
        genomeQuery.addChromosome(new Chromosome("chr1",
                new Track(queryIntervals),
                startIndexQuery,
                endIndexQuery));
    }

    @Test
    public void shouldComputeProjectionTestOnGenome() {
        assertThat(countProjection(genomeReference, genomeQuery), is(6L));
    }

    @Test
    public void shouldComputeProjectionTestOnTrackMaps() {
        Map<String, Track> referenceMap = new HashMap<>();
        genomeReference.getChromosomes().forEach(
                x -> referenceMap.put(x.getName(), x.getTrack())
        );
        Map<String, Track> queryMap = new HashMap<>();
        genomeQuery.getChromosomes().forEach(
                x -> queryMap.put(x.getName(), x.getTrack())
        );
        assertThat(countProjection(referenceMap, queryMap), is(6L));
    }

}