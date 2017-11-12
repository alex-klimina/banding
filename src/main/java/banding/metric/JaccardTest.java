package banding.metric;

import banding.Interval;
import banding.IntervalReader;

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
                    .flatMap(q -> intervalAndTrackIntersection(q, referenceIntervals));
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
            unionTrackAndInterval(unionTrack, currentInterval);
        }

        Deque<Interval> tail;
        if (!queryDeque.isEmpty()) {
            tail = queryDeque;
        } else {
            tail = referenceDeque;
        }

        while ((!tail.isEmpty())) {
            currentInterval = tail.pollFirst();
            unionTrackAndInterval(unionTrack, currentInterval);
        }
        return unionTrack;
    }

    private static void unionTrackAndInterval(Deque<Interval> unionTrack, Interval interval) {
        if (areIntervalIntersected(unionTrack.getLast(), interval)) {
            Interval lastFromUnionTrack = unionTrack.pollLast();
            Interval intervalsUnion = intervalsUnion(lastFromUnionTrack, interval);
            unionTrack.addLast(intervalsUnion);
        } else {
            unionTrack.addLast(interval);
        }
    }

    static Stream<Interval> intervalAndTrackIntersection(Interval interval, Queue<Interval> track) {
        return track.stream()
                .map(t -> intervalIntersection(interval, t))
                .filter(x -> x.getStartIndex()!=-1);
    }

    static Interval intervalIntersection(Interval interval1, Interval interval2) {
        Interval intervalUnion = new Interval("intersection_" + interval1.getName() + "_" + interval2.getName());
        if (areIntervalIntersected(interval1, interval2)) {
            intervalUnion.setStartIndex(Math.max(interval1.getStartIndex(), interval2.getStartIndex()));
            intervalUnion.setEndIndex(Math.min(interval1.getEndIndex(), interval2.getEndIndex()));
            return intervalUnion;
        } else {
            return new Interval(-1,-1);
        }
    }

    static Interval intervalsUnion(Interval interval1, Interval interval2) {
        if (areIntervalIntersected(interval1, interval2)) {
            Interval intervalUnion = new Interval("union_" + interval1.getName() + "_" + interval2.getName());
            intervalUnion.setStartIndex(Math.min(interval1.getStartIndex(), interval2.getStartIndex()));
            intervalUnion.setEndIndex(Math.max(interval1.getEndIndex(), interval2.getEndIndex()));
            return intervalUnion;
        } else {
            throw new RuntimeException("Intervals are not intersected.");
        }
    }

    static boolean isPointInInterval(int point, Interval interval) {
        return ((interval.getStartIndex() <= point) && (point <= interval.getEndIndex()));
    }

    static boolean areIntervalIntersected(Interval interval1, Interval interval2) {
        if ((isPointInInterval(interval1.getStartIndex(), interval2) ||
                isPointInInterval(interval1.getEndIndex(), interval2) ||
                isPointInInterval(interval2.getStartIndex(), interval1) ||
                isPointInInterval(interval2.getEndIndex(), interval1))) {
            return true;
        } else return false;
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
