package banding;

import java.io.IOException;
import java.util.Queue;

public class ProjectionTest {

    static long countProjection(Queue<Interval> queryIntervals, Queue<Interval> referenceIntervals) {
        return queryIntervals.stream()
                .map(ProjectionTest::middleOfInterval)
                .filter(x -> isPointInAnyIntervalOf(x, referenceIntervals))
                .count();
    }

    private static boolean isPointInAnyIntervalOf(int point, Queue<Interval> intervals) {
        return intervals.stream().anyMatch(x -> isPointInInterval(point, x));
    }

    private static boolean isPointInInterval(int point, Interval interval) {
        return ((interval.getStartIndex() <= point) && (point <= interval.getEndIndex()));
    }

    private static int middleOfInterval(Interval interval) {
        return interval.getStartIndex() + (interval.getEndIndex() - interval.getStartIndex())/2;
    }


    public static void main(String[] args) throws IOException {
        String ref = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/main/resources/hgTables_ref";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/main/resources/hgTables_CpG";

        Queue<Interval> referenceIntervals = new IntervalReader(ref).read();
        Queue<Interval> queryIntervals = new IntervalReader(query).read();

        System.out.println(ProjectionTest.countProjection(queryIntervals, referenceIntervals));
    }
}
