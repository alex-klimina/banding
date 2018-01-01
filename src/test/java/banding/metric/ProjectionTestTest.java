package banding.metric;

import banding.IntervalReader;
import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.entity.Track;
import org.junit.Test;

import java.io.IOException;
import java.util.Deque;
import java.util.Queue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProjectionTestTest {

    @Test
    public void shouldProjectionTestReturn5() throws IOException {
        String reference = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/query.txt";

        Queue<Interval> referenceIntervals = new IntervalReader(reference).read();
        Queue<Interval> queryIntervals = new IntervalReader(query).read();

        assertThat(ProjectionTest.countProjection(referenceIntervals, queryIntervals), is(6L));
    }

    @Test
    public void shouldComputeProjectionTestOnGenome() throws IOException {
        String reference = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/query.txt";

        Deque<Interval> referenceIntervals = new IntervalReader(reference).read();
        Deque<Interval> queryIntervals = new IntervalReader(query).read();

        Genome genomeReference = new Genome();
        long startIndexReference = referenceIntervals.getFirst().getStartIndex();
        long endIndexReference = referenceIntervals.getLast().getEndIndex();
        genomeReference.addChromosome(new Chromosome("chr1",
                new Track(referenceIntervals),
                startIndexReference,
                endIndexReference));

        Genome genomeQuery = new Genome();
        long startIndexQuery = queryIntervals.getFirst().getStartIndex();
        long endIndexQuery = queryIntervals.getLast().getEndIndex();
        genomeQuery.addChromosome(new Chromosome("chr1",
                new Track(queryIntervals),
                startIndexQuery,
                endIndexQuery));

        assertThat(ProjectionTest.countProjection(genomeReference, genomeQuery), is(6L));
    }

}