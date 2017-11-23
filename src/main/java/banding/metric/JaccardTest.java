package banding.metric;

import banding.IntervalReader;
import banding.entity.Interval;
import banding.entity.Track;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

public class JaccardTest {

    public static double computeJaccardStatisticForChromosome(Deque<Interval> queryIntervals, Deque<Interval> referenceIntervals) {
        Integer intersection = getIntersectionValue(queryIntervals, referenceIntervals);
        Integer union = getUnionValue(queryIntervals, referenceIntervals);
        return (double) intersection / (double) union;
    }

    static Integer getUnionValue(Deque<Interval> queryIntervals, Deque<Interval> referenceIntervals) {
        return Track.tracksUnion(queryIntervals, referenceIntervals).stream()
                    .map(Interval::getLength)
                    .collect((Collectors.summingInt((Integer::valueOf))));
    }

    static Integer getIntersectionValue(Deque<Interval> queryIntervals, Deque<Interval> referenceIntervals) {
        return Track.trackIntersection(queryIntervals, referenceIntervals)
                    .map(Interval::getLength)
                    .collect((Collectors.summingInt((Integer::valueOf))));
    }

    public static void main(String[] args) throws IOException {
        String ref = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/main/resources/hgTables_ref";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/main/resources/hgTables_CpG";

        Deque<Interval> referenceIntervals = new IntervalReader(ref).read();
        Deque<Interval> queryIntervals = new IntervalReader(query).read();

        referenceIntervals = new ArrayDeque<>(referenceIntervals.stream()
                .filter(x -> x.getName().equals("chr1"))
                .collect(Collectors.toList()));

        queryIntervals = new ArrayDeque<>(queryIntervals.stream()
                .filter(x -> x.getName().equals("chr1"))
                .collect(Collectors.toList()));

        System.out.println(JaccardTest.computeJaccardStatisticForChromosome(queryIntervals, referenceIntervals));
    }
}
