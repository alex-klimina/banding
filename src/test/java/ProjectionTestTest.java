import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ProjectionTestTest {

    @Test
    public void shouldProjectionTestReturn5() throws IOException {
        String reference = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        String query = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/query.txt";

        Queue<Interval> referenceIntervals = new IntervalReader(reference).read();
        Queue<Interval> queryIntervals = new IntervalReader(query).read();

        assertThat(ProjectionTest.countProjection(queryIntervals, referenceIntervals), is(5));
    }

}