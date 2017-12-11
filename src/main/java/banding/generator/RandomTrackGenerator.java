package banding.generator;

import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.entity.Track;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomTrackGenerator {

    public static Genome generateGenomeByReferenceLike(Genome reference, Genome query) {
        Genome generatedGenome = new Genome();
        for (Chromosome c: reference.getChromosomes()) {
            Chromosome randomChromosome = generateRandomChromosomeByReferenceLike(c, query.getChromosome(c.getName()));
            generatedGenome.addChromosome(randomChromosome);
        }
        return generatedGenome;
    }

    public static Map<String, Track> generateChromosomeSetByReferenceLike(
            Map<String, Track> reference,
            Map<String, Track> query) {
        Map<String, Track> generatedChromosomeSet = new HashMap<>();

        for (Map.Entry<String, Track> entry: reference.entrySet()) {
            Track randomTrack = generateRandomTrackByReferenceLike(reference.get(entry.getKey()), query.get(entry.getKey()));
            generatedChromosomeSet.put(entry.getKey(), randomTrack);
        }
        return generatedChromosomeSet;
    }

    public static Chromosome generateRandomChromosomeByReferenceLike(Chromosome reference, Chromosome query) {
        Track generateTrack= generateRandomTrackByReferenceLike(reference.getTrack(), query.getTrack(), reference.getStart(), reference.getEnd());
        return new Chromosome(query.getName(), generateTrack, reference.getStart(), reference.getEnd());
    }

    public static Track generateRandomTrackByReferenceLike(Track reference, Track query) {
        long startReference = reference.getTrackStart();
        long endReference = reference.getTrackEnd();
        return generateRandomTrackByReferenceLike(reference, query, startReference, endReference);
    }

    public static Track generateRandomTrackByReferenceLike(Track reference, Track query, long from, long to) {

        Track generatedTrack = new Track();

        for (Interval interval: query.getIntervals()) {
            boolean intersection = true;
            Interval randomInterval = null;
            while (intersection) {
                randomInterval = generateRandomInterval(interval.getLength(), from, to);
                intersection = Track.areIntervalAndTrackIntersect(randomInterval, generatedTrack);
            }
            generatedTrack.addInterval(randomInterval);
        }

        return generatedTrack;
    }

    public static Track generateRandomTrackLike(int length, Track track) {
        Track generatedTrack = new Track();

        for (Interval i: track.getIntervals()) {
            boolean intersection = true;
            Interval randomInterval = null;
            while (intersection) {
                randomInterval = generateRandomInterval(i.getLength(), 0, length);
                intersection = Track.areIntervalAndTrackIntersect(randomInterval, generatedTrack);
            }
            generatedTrack.addInterval(randomInterval);
        }

        return generatedTrack;
    }

    static Interval generateRandomInterval(Interval interval, Track track) {
        return generateRandomInterval(interval, track.getTrackStart(), track.getTrackEnd());
    }

    static Interval generateRandomInterval(Interval interval, long from, long to) {
        return generateRandomInterval(interval.getLength(), from, to);
    }

    static Interval generateRandomInterval(long length, long from, long to) {
        if ( (from + length) > to ) {
            throw new RuntimeException("It's impossible create an interval with length " + length
                    + " in track from " + from + " to " + to);
        }
        // if there is only one possibility for interval with that length
        if ( (from + length) == to ) {
            return new Interval(from, to);
        }

        Random random = new Random();
        Interval interval = null;
        while (interval == null) {
            long start = from + ThreadLocalRandom.current().nextLong(to);
            if ((start + length) <= to) {
                interval = new Interval(start, start + length);
            }
        }
        return interval;
    }
}
