package banding.metric;

import banding.Interval;
import banding.IntervalReader;

import java.io.IOException;
import java.util.Queue;

import static banding.Track.isPointInAnyIntervalOf;

public class ProjectionTest {

    static long countProjection(Queue<Interval> queryIntervals, Queue<Interval> referenceIntervals) {
        return queryIntervals.stream()
                .map(Interval::middleOfInterval)
                .filter(x -> isPointInAnyIntervalOf(x, referenceIntervals))
                .count();
    }

    public static void main(String[] args) throws IOException {
        String ref = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/main/resources/hgTables_ref";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/main/resources/hgTables_CpG";

        Queue<Interval> referenceIntervals = new IntervalReader(ref).read();
        Queue<Interval> queryIntervals = new IntervalReader(query).read();

        System.out.println(ProjectionTest.countProjection(queryIntervals, referenceIntervals));
    }
}
