package banding;

import banding.entity.Interval;
import banding.entity.Track;
import banding.metric.JaccardTest;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.col;

public class Main {

    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .master("local[1]")
                .appName("Banding")
                .getOrCreate();

        DataFrameReader dataFrameReader = spark.read()
                .format("com.databricks.spark.csv")
                .option("delimiter", "\t")
                .option("inferSchema", "true")
                .option("header", "true");

        String referencePath =  "src/main/resources/hgTables_ref.csv";
        Map<String, Track> referenceMap = readReferenceTrackMapFromFile(dataFrameReader, referencePath);
        String queryPath = "src/main/resources/hgTables_CpG.csv";
        Map<String, Track> queryMap = readQueryTrackMapFromFile(dataFrameReader, queryPath);

        double jaccardStatistic = JaccardTest.computeJaccardStatisticForChromosomeSet(referenceMap, queryMap);
        System.out.println(jaccardStatistic);
    }

    private static Map<String, Track> readQueryTrackMapFromFile(DataFrameReader dataFrameReader, String path) {
        String chrName = "chr1";
        Dataset<Row> queryDataset = dataFrameReader
                .load(path)
                .filter(col("chrom").equalTo(chrName))
                .select("chrom", "chromStart", "chromEnd");

        // TODO remove List<Row> rows and do map() by Spark API?
        List<Row> rows = queryDataset.collectAsList();
        Map<String, List<Interval>> map = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.groupingBy(Interval::getName,
                        Collectors.mapping(x -> new Interval(x.getStartIndex(), x.getEndIndex()), Collectors.toList())));

        Map<String, Track> trackMap = new HashMap<>();
        map.entrySet().stream()
                .forEach(entry -> trackMap.put(entry.getKey(), new Track(entry.getValue())));

        return trackMap;
    }

    private static Track readQueryTrackFromFile(DataFrameReader dataFrameReader, String path) {
        List<Row> rows;
        String chrName = "chr1";
        Dataset<Row> queryDataset = dataFrameReader
                .load(path)
                .filter(col("chrom").equalTo(chrName))
                .select("chrom", "chromStart", "chromEnd");

        // TODO remove List<Row> rows and do map() by Spark API?
        rows = queryDataset.collectAsList();
        Deque<Interval> intervalQuery = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.toCollection(ArrayDeque::new));

        return new Track(intervalQuery);
    }

    private static Map<String, Track> readReferenceTrackMapFromFile(DataFrameReader dataFrameReader, String path) {
        String chrName = "chr1";
        Dataset<Row> referenceDataset = dataFrameReader
                .load(path)
                .filter(col("chrom").equalTo(chrName))
                .filter(col("gieStain").equalTo("gpos100")
                        .or(col("gieStain").equalTo("gpos75")))
                .select("chrom", "chromStart", "chromEnd", "gieStain");

        // TODO remove List<Row> rows and do map() by Spark API?
        List<Row> rows = referenceDataset.collectAsList();
        Map<String, List<Interval>> map = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.groupingBy(Interval::getName,
                        Collectors.mapping(x -> new Interval(x.getStartIndex(), x.getEndIndex()), Collectors.toList())));

        Map<String, Track> trackMap = new HashMap<>();
        map.entrySet().stream()
                .forEach(entry -> trackMap.put(entry.getKey(), new Track(entry.getValue())));

        return trackMap;
    }

}
