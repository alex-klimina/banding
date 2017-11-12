package banding.metric;

import banding.IntervalReader;
import banding.entity.Interval;
import banding.entity.Track;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JaccardTest {

    public static double computeJaccardStatistic(Deque<Interval> queryIntervals, Deque<Interval> referenceIntervals) {
        Integer intersection = getIntersectionValue(queryIntervals, referenceIntervals);
        Integer union = getUnionValue(queryIntervals, referenceIntervals);
        return (double) intersection / (double) union;
    }

    static Integer getUnionValue(Deque<Interval> queryIntervals, Deque<Interval> referenceIntervals) {
        return tracksUnion(queryIntervals, referenceIntervals).stream()
                    .map(Interval::getLength)
                    .collect((Collectors.summingInt((Integer::valueOf))));
    }

    static Integer getIntersectionValue(Deque<Interval> queryIntervals, Deque<Interval> referenceIntervals) {
        return trackIntersection(queryIntervals, referenceIntervals)
                    .map(Interval::getLength)
                    .collect((Collectors.summingInt((Integer::valueOf))));
    }

    public static Stream<Interval> trackIntersection(Queue<Interval> queryIntervals, Queue<Interval> referenceIntervals) {
        return queryIntervals.stream()
                    .flatMap(q -> Track.intervalAndTrackIntersection(q, referenceIntervals));
    }

    static Deque<Interval> tracksUnion(Deque<Interval> queryDeque, Deque<Interval> referenceDeque) {
        Deque<Interval> unionTrack = new ArrayDeque<>();

        Interval currentInterval;
        if (queryDeque.getFirst().getStartIndex() <= referenceDeque.getFirst().getStartIndex()) {
            currentInterval = queryDeque.pollFirst();
        } else {
            currentInterval = referenceDeque.pollFirst();
        }
        unionTrack.addLast(currentInterval);

        while (!queryDeque.isEmpty() && !referenceDeque.isEmpty()) {
            if (queryDeque.getFirst().getStartIndex() <= referenceDeque.getFirst().getStartIndex()) {
                currentInterval = queryDeque.pollFirst();
            } else {
                currentInterval = referenceDeque.pollFirst();
            }
            Track.unionTrackAndInterval(unionTrack, currentInterval);
        }

        Deque<Interval> tail;
        if (!queryDeque.isEmpty()) {
            tail = queryDeque;
        } else {
            tail = referenceDeque;
        }

        while ((!tail.isEmpty())) {
            currentInterval = tail.pollFirst();
            Track.unionTrackAndInterval(unionTrack, currentInterval);
        }
        return unionTrack;
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

        System.out.println(JaccardTest.computeJaccardStatistic(queryIntervals, referenceIntervals));
    }
}
