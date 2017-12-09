package banding;

import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.entity.Track;
import banding.generator.RandomTrackGenerator;
import banding.metric.JaccardTest;
import banding.metric.ProjectionTest;
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
import java.util.stream.IntStream;

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
        // TODO read to Genom
        Genome reference = readReferenceTrackMapFromFile(dataFrameReader, referencePath);

        String queryPath = "src/main/resources/hgTables_CpG.csv";
        // TODO read to Genom
        Map<String, Track> queryMap = readQueryTrackMapFromFile(dataFrameReader, queryPath);

        //TODO work with Genom
        computeCoverageAndGenomLength(reference);

        printExpectedDistributionParameters(reference, queryMap);


        int n = 10;

        //TODO work with Genom
        generateRandomChromosomeSetsAndComputeProjectionTest(reference, queryMap, n);
        generateRandomTrackAndComputeJaccardStatistic(reference, queryMap);
        generateRandomChromosomeSetsAndComputeJaccardStatistic(reference, queryMap);

    }

    private static void printExpectedDistributionParameters(Map<String, Track> referenceMap, Map<String, Track> queryMap) {
        System.out.println("Expected distribution: ");
        double p = ((double) referenceMap.get("chr1").getCoverage() -1) /( (double) referenceMap.get("chr1").getLength() - 1);
        System.out.println("p = coverage / length = "
                + referenceMap.get("chr1").getCoverage() + " / " + referenceMap.get("chr1").getLength() + " = " + p);
        System.out.println("Expected average: " + p * queryMap.get("chr1").getNumberOfIntervals());
    }

    private static void computeCoverageAndGenomLength(Genome reference) {
        long length = 0;
        long coverage = 0;
        for (Chromosome c: reference.getChromosomes()) {
            length += c.getLength();
            coverage += c.getCoverage();
        }
        System.out.println("Coverage the reference: " + coverage);
        System.out.println("Length the reference: " + length);
        double p = (double) coverage / (double) length;
        System.out.println("p for one mapped point: " + p);
    }

    private static void generateRandomChromosomeSetsAndComputeProjectionTest(Map<String, Track> referenceMap, Map<String, Track> queryMap, int numberOfExperiments) {

        System.out.println("======");
        System.out.println("Whole genome");
        System.out.println("projectionCount for CpG:");
        System.out.println(ProjectionTest.countProjection(referenceMap, queryMap));

        int capacity = numberOfExperiments;
        List<Integer> stats = IntStream.range(0, capacity).boxed()
                .parallel()
                .map(x -> getProjectionCountForRandomChromosome(referenceMap, queryMap))
                .collect(Collectors.toList());

        System.out.println("projectionCount for random tracks by CpG: ");
        DoubleSummaryStatistics summaryStatistics = stats.stream().collect(Collectors.summarizingDouble(Double::valueOf));
        System.out.println(summaryStatistics);
        System.out.println("all values:");
        System.out.println(stats);
    }

    private static int getProjectionCountForRandomChromosome(Map<String, Track> referenceMap, Map<String, Track> queryMap) {
        Map<String, Track> randomChromosomes = RandomTrackGenerator.generateChromosomeSetByReferenceLike(referenceMap, queryMap);
        return ProjectionTest.countProjection(referenceMap, randomChromosomes);
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

    private static Genome readReferenceTrackMapFromFile(DataFrameReader dataFrameReader, String path) {
        Dataset<Row> referenceDatasetStartEnd = dataFrameReader
                .load(path)
                .select("chrom", "chromStart", "chromEnd");
        List<Row> rows = referenceDatasetStartEnd.collectAsList();
        Map<String, List<PairStartEnd>> mapStartEnd = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.groupingBy(Interval::getName,
                        Collectors.mapping(x -> new PairStartEnd(x.getStartIndex(), x.getEndIndex()), Collectors.toList())));

        Dataset<Row> referenceDataset = dataFrameReader
                .load(path)
                .filter(col("gieStain").equalTo("gpos100")
                        .or(col("gieStain").equalTo("gpos75")))
                .select("chrom", "chromStart", "chromEnd", "gieStain");

        // TODO remove List<Row> rows and do map() by Spark API?
        rows = referenceDataset.collectAsList();
        Map<String, List<Interval>> map = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.groupingBy(Interval::getName,
                        Collectors.mapping(x -> new Interval(x.getStartIndex(), x.getEndIndex()), Collectors.toList())));

        Map<String, Track> trackMap = new HashMap<>();
        map.entrySet()
                .forEach(entry -> trackMap.put(entry.getKey(), new Track(entry.getValue())));

        ArrayList<Chromosome> chromosomes = new ArrayList<>();
        trackMap.entrySet()
                .forEach(x -> chromosomes.add(
                        new Chromosome(x.getKey(),
                                x.getValue(),
                                mapStartEnd.get(x.getKey()).get(0).start,
                                mapStartEnd.get(x.getKey()).get(mapStartEnd.get(x.getKey()).size() - 1).end)));
        return new Genome(chromosomes);
    }

    private static class PairStartEnd {
        int start;
        int end;

        public PairStartEnd(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

}
