package banding.entity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import static banding.entity.Interval.isPointInInterval;

public class Track {

    private Deque<Interval> intervals;

    public Track(Deque<Interval> intervals) {
        this.intervals = intervals;
    }

    public Track(List<Interval> intervals) {
        this.intervals = new ArrayDeque<>(intervals);
    }

    public Track() {
        this.intervals = new ArrayDeque<>();
    }

    public int getTrackStart() {
        return intervals.getFirst().getStartIndex();
    }

    public int getTrackEnd() {
        return intervals.getLast().getEndIndex();
    }

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

    public static Stream<Interval> trackIntersection(Queue<Interval> queryIntervals, Queue<Interval> referenceIntervals) {
        return queryIntervals.stream()
                    .flatMap(q -> intervalAndTrackIntersection(q, referenceIntervals));
    }

    public static Deque<Interval> tracksUnion(Deque<Interval> queryDeque, Deque<Interval> referenceDeque) {
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

    public Deque<Interval> getIntervals() {
        return intervals;
    }

    public Track addInterval(Interval interval) {
        this.intervals.add(interval);
        return this;
    }

    public int getLength() {
        return getTrackEnd() - getTrackStart();
    }

    public int getNumberOfIntervals() {
        return intervals.size();
    }
}
