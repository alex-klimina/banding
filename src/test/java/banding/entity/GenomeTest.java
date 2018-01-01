package banding.entity;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class GenomeTest {

    Track track1;
    Track track2;
    List<Chromosome> chromosomes;
    Genome genome;

    @Before
    public void initialize() {
        track1 = new Track();
        track1.addInterval(0, 3)
                .addInterval(5, 8)
                .addInterval(10, 15);
        Chromosome chr1 = new Chromosome("chr1", track1, 0, 20);

        track2 = new Track();
        track2.addInterval(0, 3)
                .addInterval(5, 8)
                .addInterval(10, 15);
        Chromosome chr2 = new Chromosome("chr2", track2, 0, 30);

        chromosomes = new ArrayList<>();
        chromosomes.add(chr1);
        chromosomes.add(chr2);

        genome = new Genome();
        genome.addChromosome(chr1);
        genome.addChromosome(chr2);
    }

    @Test
    public void shouldCheckCreatingGenome() {
        assertThat(genome.getChromosomes(), is(chromosomes));
    }

    @Test
    public void shouldGetLengthOfGenome() {
        assertThat(genome.getLength(), is(52L));
    }

    @Test
    public void shouldGetNumberOfIntervalsInGenome() {
        assertThat(genome.getNumberOfIntervals(), is(6L));
    }

}