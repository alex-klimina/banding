import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ProjectionTestTest {

    @Test
    public void shouldProjectionTestReturn5() throws IOException {
        String reference = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/query.txt";

        List<Interval> referenceIntervals = new IntervalReader(reference).read();
        List<Interval> queryIntervals = new IntervalReader(query).read();

        assertThat(ProjectionTest.countProjection(queryIntervals, referenceIntervals), is(5));
    }

    @Test
    public void printMiddles() throws IOException {
        String reference = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/query.txt";

//        List<Interval> referenceIntervals = new IntervalReader(reference).read();
        List<Interval> queryIntervals = new IntervalReader(query).read();

        queryIntervals.stream()
//                .map(ProjectionTest::middleOfInterval)
                .forEach(System.out::println);

        queryIntervals.stream()
                .map(ProjectionTest::middleOfInterval)
                .forEach(System.out::println);

    }

}