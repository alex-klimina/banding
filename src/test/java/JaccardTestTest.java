import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class JaccardTestTest {


    @Test
    public void shouldJaccardTestReturn0643() throws IOException {
        String reference = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/query.txt";

        List<Interval> referenceIntervals = new IntervalReader(reference).read();
        List<Interval> queryIntervals = new IntervalReader(query).read();

        assertThat(JaccardTest.compute(queryIntervals, referenceIntervals), is(0.643));
    }


    @Test
    public void print() throws IOException {
        String reference = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/query.txt";

        List<Interval> referenceIntervals = new IntervalReader(reference).read();
        List<Interval> queryIntervals = new IntervalReader(query).read();

        System.out.println("==== union ");

        queryIntervals.stream()
                .map(interval -> JaccardTest.intervalAndTrackUnion(interval, referenceIntervals))
                .forEach(System.out::println);

        System.out.println("==== intersection ");

        queryIntervals.stream()
                .map(interval -> JaccardTest.intervalAndTrackIntersection(interval, referenceIntervals))
                .forEach(System.out::println);


    }
}