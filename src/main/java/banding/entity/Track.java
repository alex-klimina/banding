package banding.entity;

import java.util.Queue;

import static banding.entity.Interval.isPointInInterval;

public class Track {

    public static boolean isPointInAnyIntervalOf(int point, Queue<Interval> intervals) {
        return intervals.stream().anyMatch(x -> isPointInInterval(point, x));
    }
}
