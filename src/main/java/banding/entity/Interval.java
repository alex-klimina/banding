package banding.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode (exclude = "name")
@AllArgsConstructor
public class Interval implements Comparable<Interval> {
    private String name;
    private long startIndex;
    private long endIndex;

    public Interval(long startIndex, long endIndex) {
        this(null, startIndex, endIndex);
    }

    public Interval(String name) {
        this.name = name;
    }

    public static long middleOfInterval(Interval interval) {
        return interval.getStartIndex() + (interval.getEndIndex() - interval.getStartIndex())/2;
    }

    public static boolean isPointInInterval(long point, Interval interval) {
        return ((interval.getStartIndex() <= point) && (point <= interval.getEndIndex()));
    }

    public static boolean areIntervalsIntersected(Interval interval1, Interval interval2) {
        if ((isPointInInterval(interval1.getStartIndex(), interval2) ||
                isPointInInterval(interval1.getEndIndex(), interval2) ||
                isPointInInterval(interval2.getStartIndex(), interval1) ||
                isPointInInterval(interval2.getEndIndex(), interval1))) {
            return true;
        } else return false;
    }

    public static Interval intervalsUnion(Interval interval1, Interval interval2) {
        if (areIntervalsIntersected(interval1, interval2)) {
            Interval intervalUnion = new Interval("union_" + interval1.getName() + "_" + interval2.getName());
            intervalUnion.setStartIndex(Math.min(interval1.getStartIndex(), interval2.getStartIndex()));
            intervalUnion.setEndIndex(Math.max(interval1.getEndIndex(), interval2.getEndIndex()));
            return intervalUnion;
        } else {
            throw new RuntimeException("Intervals are not intersected.");
        }
    }

    public static Interval intervalIntersection(Interval interval1, Interval interval2) {
        Interval intervalUnion = new Interval("intersection_" + interval1.getName() + "_" + interval2.getName());
        if (areIntervalsIntersected(interval1, interval2)) {
            intervalUnion.setStartIndex(Math.max(interval1.getStartIndex(), interval2.getStartIndex()));
            intervalUnion.setEndIndex(Math.min(interval1.getEndIndex(), interval2.getEndIndex()));
            return intervalUnion;
        } else {
            return new Interval(-1,-1);
        }
    }

    public long getLength() {
        return endIndex - startIndex + 1;
    }

    @Override
    public int compareTo(Interval o) {
        return (int) (this.getStartIndex() - o.getStartIndex());
    }
}


