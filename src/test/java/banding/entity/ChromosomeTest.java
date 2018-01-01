package banding.entity;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ChromosomeTest {

    Track track1;
    Chromosome chr1;

    @Before
    public void initialize() {
        track1 = new Track();
        track1.addInterval(0, 3)
                .addInterval(5, 8)
                .addInterval(10, 15);
        chr1 = new Chromosome("chr1", track1, 0, 20);
    }

    @Test
    public void shouldCreateChromosomeWithAllAgrs() {
        assertThat(chr1.getName(), is("chr1"));
        assertThat(chr1.getTrack(), is(track1));
        assertThat(chr1.getStart(), is(0L));
        assertThat(chr1.getEnd(), is(20L));
    }

    @Test
    public void shouldGetLengthOfChromosome() {
        assertThat(chr1.getLength(), is(21L));
    }

    @Test
    public void shouldGetCoverage() {
        assertThat(chr1.getCoverage(), is(4L + 4L + 6L));
    }

    @Test
    public void shouldGetNumberOfInterval() {
        assertThat(chr1.getNumberOfIntervals(), is(3));
    }

    @Test
    public void shouldCheckEquals() {
        Chromosome chr1Copy = new Chromosome("chr1", track1, 0, 20);
        assertEquals(chr1, chr1Copy);
    }
}