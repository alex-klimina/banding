package banding.metric;

import banding.IntervalReader;
import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.entity.Track;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JaccardTest {


    public static double computeJaccardStatisticForGenome(Genome reference, Genome query) {
        HashMap<String, Track> referenceMap = new HashMap();
        for (Chromosome c: reference.getChromosomes()) {
            referenceMap.put(c.getName(), c.getTrack());
        }
        HashMap<String, Track> queryMap = new HashMap();
        for (Chromosome c: query.getChromosomes()) {
            queryMap.put(c.getName(), c.getTrack());
        }
        return computeJaccardStatisticForChromosomeSet(referenceMap, queryMap);
    }

    public static double computeJaccardStatisticForChromosome(Track query, Track reference) {
        return computeJaccardStatisticForChromosome(query.getIntervals(), reference.getIntervals());
    }

    public static double computeJaccardStatisticForChromosome(Deque<Interval> queryIntervals, Deque<Interval> referenceIntervals) {
        Long intersection = getIntersectionValue(queryIntervals, referenceIntervals);
        Long union = getUnionValue(queryIntervals, referenceIntervals);
        return (double) intersection / (double) union;
    }

    public static double computeJaccardStatisticForChromosomeSet(Map<String, Track> referenceMap, Map<String, Track> queryMap) {
        Long intersection = getIntersectionValueForTrackSet(referenceMap, queryMap);
        Long union = getUnionValueForTrackSet(referenceMap, queryMap);
        return (double) intersection / (double) union;
    }

    static Long getUnionValue(Deque<Interval> queryIntervals, Deque<Interval> referenceIntervals) {
        return Track.tracksUnion(queryIntervals, referenceIntervals).stream()
                    .map(Interval::getLength)
                    .collect((Collectors.summingLong((Long::valueOf))));
    }

    static Long getUnionValue(Track queryIntervals, Track referenceIntervals) {
        return Track.tracksUnion(queryIntervals.getIntervals(), referenceIntervals.getIntervals()).stream()
                    .map(Interval::getLength)
                    .collect((Collectors.summingLong((Long::valueOf))));
    }

    static Long getUnionValueForTrackSet(Map<String, Track> referenceMap, Map<String, Track> queryMap) {
        return referenceMap.keySet().stream()
                .map(name -> getUnionValue(queryMap.get(name), referenceMap.get(name)))
                .mapToLong(Long::valueOf)
                .sum();
    }

    static Long getIntersectionValue(Deque<Interval> queryIntervals, Deque<Interval> referenceIntervals) {
        return Track.trackIntersection(queryIntervals, referenceIntervals)
                    .map(Interval::getLength)
                    .collect((Collectors.summingLong((Long::valueOf))));
    }

    static Long getIntersectionValue(Track queryIntervals, Track referenceIntervals) {
        return Track.trackIntersection(queryIntervals.getIntervals(), referenceIntervals.getIntervals())
                .map(Interval::getLength)
                .collect((Collectors.summingLong((Long::valueOf))));
    }

    static Long getIntersectionValueForTrackSet(Map<String, Track> referenceMap, Map<String, Track> queryMap) {
        return referenceMap.keySet().stream()
                .map(name -> getIntersectionValue(queryMap.get(name), referenceMap.get(name)))
                .mapToLong(Long::valueOf)
                .sum();
    }

}
