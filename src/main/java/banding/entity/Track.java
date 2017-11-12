package banding.entity;

import java.util.Deque;
import java.util.Queue;
import java.util.stream.Stream;

import static banding.entity.Interval.isPointInInterval;

public class Track {

    public static boolean isPointInAnyIntervalOf(int point, Queue<Interval> intervals) {
        return intervals.stream().anyMatch(x -> isPointInInterval(point, x));
    }

    public static Stream<Interval> intervalAndTrackIntersection(Interval interval, Queue<Interval> track) {
        return track.stream()
                .map(t -> Interval.intervalIntersection(interval, t))
                .filter(x -> x.getStartIndex()!=-1);
    }

    public static void unionTrackAndInterval(Deque<Interval> unionTrack, Interval interval) {
        if (Interval.areIntervalsIntersected(unionTrack.getLast(), interval)) {
            Interval lastFromUnionTrack = unionTrack.pollLast();
            Interval intervalsUnion = Interval.intervalsUnion(lastFromUnionTrack, interval);
            unionTrack.addLast(intervalsUnion);
        } else {
            unionTrack.addLast(interval);
        }
    }
}
