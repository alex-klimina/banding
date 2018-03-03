package banding.metric;

import banding.IntervalReader;
import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.entity.Track;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;

import static banding.entity.Interval.isPointInInterval;
import static banding.entity.Track.isPointInAnyIntervalOf;

public class ProjectionTest {


    public static long countProjection(Genome reference, Genome query) {
        int projectionCount = 0;

        for (Chromosome chromosome: reference.getChromosomes()) {
            projectionCount += countProjection(chromosome.getTrack(), query.getChromosome(chromosome.getName()).getTrack());
        }
        return projectionCount;
    }

    public static long countProjection(Map<String, Track> referenceMap, Map<String, Track> queryMap) {
        int projectionCount = 0;

        for (Map.Entry<String, Track> entry: referenceMap.entrySet()) {
            projectionCount += countProjection(entry.getValue(), queryMap.get(entry.getKey()));
        }
        return projectionCount;
    }

    private static long countProjection(Track reference, Track query) {
        return countProjection(reference.getIntervals(), query.getIntervals());
    }

    public static long countProjection(Queue<Interval> referenceIntervals, Queue<Interval> queryIntervals) {
        return queryIntervals.stream()
                .map(Interval::middleOfInterval)
                .filter(x -> isPointInAnyIntervalOf(x, referenceIntervals))
                .count();
    }

    public static long countProjection(Interval referenceInterval, Collection<Interval> queryIntervals) {
        return queryIntervals.stream()
                .map(Interval::middleOfInterval)
                .filter(x -> isPointInInterval(x, referenceInterval))
                .count();
    }

    public static long countProjection(Interval referenceInterval, Track query) {
        return countProjection(referenceInterval, query.getIntervals());
    }

    public static long countProjection(Interval referenceInterval, Chromosome query) {
        return countProjection(referenceInterval, query.getTrack());
    }
}