import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class IntervalReader {

    final String path;

    public IntervalReader(String path) {
        this.path = path;
    }

    List<Interval> read() throws IOException {
        List<Interval> intervals = new ArrayList<>();

        Reader in = new FileReader(path);
        Iterable<CSVRecord> records = CSVFormat.RFC4180
                .withDelimiter('\t')
                .withFirstRecordAsHeader()
                .parse(in);

        for (CSVRecord record : records) {
            Interval interval = new Interval(record.get("chrom"),
                    Integer.valueOf(record.get("chromStart")),
                    Integer.valueOf(record.get("chromEnd")));
            intervals.add(interval);
        }

        return intervals;
    }

    public static void main(String[] args) throws IOException {
        String path = "/Users/alkli/Documents/Yandex.Disk/BioInstitute/banding/banding/src/test/resources/ref.txt";
        List<Interval> intervals = new IntervalReader(path).read();
        for (Interval interval: intervals) {
            System.out.println(interval);
        }
    }
}
