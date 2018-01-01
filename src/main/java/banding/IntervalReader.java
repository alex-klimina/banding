package banding;

import banding.entity.Interval;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

public class IntervalReader {

    private final String path;

    public IntervalReader(String path) {
        this.path = path;
    }

    public Deque<Interval> read() throws IOException {
        Deque<Interval> intervals = new ArrayDeque<>();

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
}
