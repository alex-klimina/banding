package banding;

import banding.entity.Interval;
import banding.entity.Track;
import banding.generator.RandomTrackGenerator;
import banding.metric.JaccardTest;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static banding.generator.RandomTrackGenerator.generateRandomTrackLike;
import static org.apache.spark.sql.functions.col;

public class Main {

    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .master("local[4]")
                .appName("Banding")
                .getOrCreate();

        DataFrameReader dataFrameReader = spark.read()
                .format("com.databricks.spark.csv")
                .option("delimiter", "\t")
                .option("inferSchema", "true")
                .option("header", "true");

        String referencePath = "src/main/resources/hgTables_ref.csv";
        Map<String, Track> referenceMap = readReferenceTrackMapFromFile(dataFrameReader, referencePath);
        String queryPath = "src/main/resources/hgTables_CpG.csv";
        Map<String, Track> queryMap = readQueryTrackMapFromFile(dataFrameReader, queryPath);

        generateRandomTrackAndComputeJaccardStatistic(referenceMap, queryMap);
        generateRandomChromosomeSetsAndComputeJaccardStatistic(referenceMap, queryMap);

    }

    private static void generateRandomTrackAndComputeJaccardStatistic(Map<String, Track> referenceMap, Map<String, Track> queryMap) {
        double jaccardStatistic;
        int chr1End = 248956422;
        Track chr1 = queryMap.get("chr1");
        int capacity = 100;
        List<Double> stats = new ArrayList<>(capacity);

        for (int i = 0; i < capacity; i++) {
            Track randomTrack = generateRandomTrackLike(chr1End, chr1);
            jaccardStatistic = JaccardTest.computeJaccardStatisticForChromosome(randomTrack, referenceMap.get("chr1"));
            stats.add(jaccardStatistic);
        }

        System.out.println("Chr1: jaccardStatistic for CpG: " + JaccardTest.computeJaccardStatisticForChromosome(queryMap.get("chr1"), referenceMap.get("chr1")));
        System.out.println("Chr1: jaccardStatistic for random tracks by CpG: \n");
        DoubleSummaryStatistics summaryStatistics = stats.stream().collect(Collectors.summarizingDouble(Double::valueOf));
        System.out.println(summaryStatistics);
    }

    private static void generateRandomChromosomeSetsAndComputeJaccardStatistic(Map<String, Track> referenceMap, Map<String, Track> queryMap) {

        System.out.println("======");
        System.out.println("Whole genome");
        double jaccardStatistic = JaccardTest.computeJaccardStatisticForChromosomeSet(referenceMap, queryMap);
        System.out.println("jaccardStatistic for CpG: " + jaccardStatistic);

        List<Double> stats;
        DoubleSummaryStatistics summaryStatistics;
        int capacity = 100;
        stats = new ArrayList<>(capacity);

        for (int i = 0; i < capacity; i++) {
            Map<String, Track> randomChomosomes = RandomTrackGenerator.generateChromosomeSetByReferenceLike(referenceMap, queryMap);
            jaccardStatistic = JaccardTest.computeJaccardStatisticForChromosomeSet(referenceMap, randomChomosomes);
            stats.add(jaccardStatistic);
        }
        System.out.println("Chr1: jaccardStatistic for random tracks by CpG: \n");
        summaryStatistics = stats.stream().collect(Collectors.summarizingDouble(Double::valueOf));
        System.out.println(summaryStatistics);
    }


    private static Map<String, Track> readQueryTrackMapFromFile(DataFrameReader dataFrameReader, String path) {
        Dataset<Row> queryDataset = dataFrameReader
                .load(path)
                .select("chrom", "chromStart", "chromEnd");

        // TODO remove List<Row> rows and do map() by Spark API?
        List<Row> rows = queryDataset.collectAsList();
        Map<String, List<Interval>> map = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.groupingBy(Interval::getName,
                        Collectors.mapping(x -> new Interval(x.getStartIndex(), x.getEndIndex()), Collectors.toList())));

        Map<String, Track> trackMap = new HashMap<>();
        map.entrySet()
                .forEach(entry -> trackMap.put(entry.getKey(), new Track(entry.getValue())));

        return trackMap;
    }

    private static Map<String, Track> readReferenceTrackMapFromFile(DataFrameReader dataFrameReader, String path) {
        Dataset<Row> referenceDataset = dataFrameReader
                .load(path)
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
        map.entrySet()
                .forEach(entry -> trackMap.put(entry.getKey(), new Track(entry.getValue())));

        return trackMap;
    }

}
