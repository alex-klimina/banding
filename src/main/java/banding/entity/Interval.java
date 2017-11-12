package banding.entity;

import java.util.Objects;

public class Interval {
    private String name;
    private int startIndex;
    private int endIndex;

    public static int middleOfInterval(Interval interval) {
        return interval.getStartIndex() + (interval.getEndIndex() - interval.getStartIndex())/2;
    }

    public static boolean isPointInInterval(int point, Interval interval) {
        return ((interval.getStartIndex() <= point) && (point <= interval.getEndIndex()));
    }

    public Interval(String name, int startIndex, int endIndex) {
        this.name = name;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public Interval(String name) {
        this.name = name;
    }

    public Interval(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public String toString() {
        return "banding.Interval{" +
                "name='" + name + '\'' +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interval interval = (Interval) o;
        return startIndex == interval.startIndex &&
                endIndex == interval.endIndex;
    }

    @Override
    public int hashCode() {

        return Objects.hash(startIndex, endIndex);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public int getLength() {
        return endIndex - startIndex;
    }
}
