package banding.entity;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static banding.entity.Interval.isPointInInterval;

@ToString
public class Track {

    @Getter
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
        Deque<Interval> queryDequeInternal = new ArrayDeque<>(queryDeque);
        Deque<Interval> referenceDequeInternal = new ArrayDeque<>(referenceDeque);

        Deque<Interval> unionTrack = new ArrayDeque<>();

        Interval currentInterval;
        if (queryDequeInternal.getFirst().getStartIndex() <= referenceDequeInternal.getFirst().getStartIndex()) {
            currentInterval = queryDequeInternal.pollFirst();
        } else {
            currentInterval = referenceDequeInternal.pollFirst();
        }
        unionTrack.addLast(currentInterval);

        while (!queryDequeInternal.isEmpty() && !referenceDequeInternal.isEmpty()) {
            if (queryDequeInternal.getFirst().getStartIndex() <= referenceDequeInternal.getFirst().getStartIndex()) {
                currentInterval = queryDequeInternal.pollFirst();
            } else {
                currentInterval = referenceDequeInternal.pollFirst();
            }
            unionTrackAndInterval(unionTrack, currentInterval);
        }

        Deque<Interval> tail;
        if (!queryDequeInternal.isEmpty()) {
            tail = queryDequeInternal;
        } else {
            tail = referenceDequeInternal;
        }

        while ((!tail.isEmpty())) {
            currentInterval = tail.pollFirst();
            unionTrackAndInterval(unionTrack, currentInterval);
        }
        return unionTrack;
    }

    public Track addInterval(int startInterval, int endInterval) {
        return addInterval(new Interval(startInterval, endInterval));
    }

    public Track addInterval(Interval interval) {
        // TODO add sorting intervals (use not deque?)
        this.intervals.add(interval);
        return this;
    }

    public int getLength() {
        return getTrackEnd() - getTrackStart();
    }

    public int getNumberOfIntervals() {
        return intervals.size();
    }

    public static boolean areIntervalAndTrackIntersect(Interval interval, Track track) {
        long count = intervalAndTrackIntersection(interval, track.getIntervals())
                .filter(i -> i.getStartIndex() != -1 && interval.getEndIndex() != -1)
                .count();
        return count != 0;
    }

    public void sortIntervals() {
        this.intervals = this.intervals.stream()
                .sorted()
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

}
