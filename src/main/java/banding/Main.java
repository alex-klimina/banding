package banding;

import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.entity.Track;
import banding.generator.RandomTrackGenerator;
import banding.metric.JaccardTest;
import banding.metric.ProjectionTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.mllib.stat.test.KolmogorovSmirnovTestResult;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static banding.generator.RandomTrackGenerator.generateRandomTrackLike;
import static org.apache.spark.sql.functions.col;

public class Main {

    private static SparkSession spark = SparkSession
            .builder()
            .master("local[4]")
            .appName("Banding")
            .getOrCreate();

    private JavaSparkContext getSparkContext() {
        return JavaSparkContext.fromSparkContext(spark.sparkContext());
    }

    public static void main(String[] args) throws IOException {

        Main main = new Main();

        DataFrameReader dataFrameReader = spark.read()
                .format("com.databricks.spark.csv")
                .option("delimiter", "\t")
                .option("inferSchema", "true")
                .option("header", "true");

        String referencePath = "src/main/resources/hgTables_ref_only_main.csv";
        Genome reference = readReferenceTrackMapFromFile(dataFrameReader, referencePath);

        String queryPath = "src/main/resources/hgTables_CpG_only_main.csv";
        Genome query = readQueryTrackMapFromFile(dataFrameReader, queryPath);

        String outputProjectionTest = "reportProjectionTest.txt";
        main.getReportForProjectionTest(reference, query, outputProjectionTest);

        String outputJaccardTest = "reportJaccardTest.txt";
        main.getReportForJaccardTest(reference, query, outputJaccardTest);

    }

    private void getReportForProjectionTest(Genome reference, Genome query, String output) throws IOException {
        File file = new File(output);

        addLineToFile(file, "Сoverage of reference: " + reference.getCoverage());
        addLineToFile(file, "Length of reference: " + reference.getLength());
        double p = ((double) reference.getCoverage()) / ((double) reference.getLength());
        addLineToFile(file, "Probability match point to reference: p = coverage/length = " + p);
        addLineToFile(file, "Number of intervals in query: " + query.getNumberOfIntervals());
        double expectedValueForBinomialDistribution = getExpectedValueForBinomialDistribution(reference, query);
        addLineToFile(file, "Expected value for binomial distribution: " + expectedValueForBinomialDistribution);
        int queryProjectionTest = ProjectionTest.countProjection(reference, query);
        addLineToFile(file, "ProjectionCount for query: "
                + queryProjectionTest);

        int n = 100;
        List<Integer> projectionTestExperiments = generateRandomChromosomeSetsAndComputeProjectionTest(reference, query, n);
        addLineToFile(file, "ProjectionCount for random tracks by query:"
                + projectionTestExperiments);

        Double mean = projectionTestExperiments.stream().collect(Collectors.averagingDouble(Double::valueOf));
        Double sumDev = projectionTestExperiments.stream()
                .map(x -> ((double) x - mean) * ((double) x - mean))
                .collect(Collectors.summingDouble(Double::valueOf));
        double sd = Math.sqrt(sumDev / n);

        JavaDoubleRDD rdd = getSparkContext().parallelize(projectionTestExperiments).mapToDouble(Double::valueOf);
        KolmogorovSmirnovTestResult result = Statistics.kolmogorovSmirnovTest(rdd, "norm", mean, sd);
        addLineToFile(file, result.toString());

        TTest tTest = new TTest();
        double tTestPValue = tTest.tTest(queryProjectionTest, getDoubleArray(projectionTestExperiments));
        addLineToFile(file, "pValue for query track: " + tTestPValue);

    }

    private void getReportForJaccardTest(Genome reference, Genome query, String output) throws IOException {
        File file = new File(output);
        addLineToFile(file, "Сoverage of reference: " + reference.getCoverage());
        addLineToFile(file, "Length of reference: " + reference.getLength());

        double queryProjectionTest = JaccardTest.computeJaccardStatisticForGenome(reference, query);
        addLineToFile(file, "JaccardTest for query: "
                + queryProjectionTest);

        int n = 100;
        List<Double> jaccardTestExperiments = generateRandomChromosomeSetsAndComputeJaccardTest(reference, query, n);
        addLineToFile(file, "JaccardTest for random tracks by query:"
                + jaccardTestExperiments);

        Double mean = jaccardTestExperiments.stream().collect(Collectors.averagingDouble(Double::valueOf));
        Double sumDev = jaccardTestExperiments.stream()
                .map(x -> ((double) x - mean) * ((double) x - mean))
                .collect(Collectors.summingDouble(Double::valueOf));
        double sd = Math.sqrt(sumDev / n);

        JavaDoubleRDD rdd = getSparkContext().parallelize(jaccardTestExperiments).mapToDouble(Double::doubleValue); //.mapToDouble(Double::valueOf);
        KolmogorovSmirnovTestResult result = Statistics.kolmogorovSmirnovTest(rdd, "norm", mean, sd);
        addLineToFile(file, result.toString());

        TTest tTest = new TTest();
        double tTestPValue = tTest.tTest(queryProjectionTest, getDoubleArray(jaccardTestExperiments));
        addLineToFile(file, "pValue for query track: " + tTestPValue);

    }

    private double[] getDoubleArray(List<? extends Number> list) {
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i).doubleValue();
        }
        return arr;
    }

    private static void addLineToFile(File file, String line) throws IOException {
        boolean append = true;
        FileUtils.writeStringToFile(file, line + "\n", append);
    }

    private static void computeProjectionTestForSeparateChromosomes(Genome reference, Genome query, int n) {
        for (Chromosome c: reference.getChromosomes()) {
            Genome tempGenome = new Genome(Collections.singletonList(c));
            System.out.println(c.getName());
            System.out.println(getExpectedValueForBinomialDistribution(
                    tempGenome,
                    new Genome(Collections.singletonList(query.getChromosome(c.getName())))));
            List<Integer> projectionTestRandomExperiments = generateRandomChromosomeSetsAndComputeProjectionTest(tempGenome, query, n);
            DoubleSummaryStatistics summaryStatistics = getStatisticForExperiments(projectionTestRandomExperiments);
            System.out.println("Summary statistic: " + summaryStatistics);
            System.out.println(projectionTestRandomExperiments);
        }
    }

    private static DoubleSummaryStatistics getStatisticForExperiments(List<Integer> experiments) {
        return experiments.stream().collect(Collectors.summarizingDouble(Double::valueOf));
    }

    private static double getExpectedValueForBinomialDistribution(Genome reference, Genome query) {
        double p = ((double) reference.getCoverage()) / ( (double) reference.getLength());
        long numberOfIntervals = query.getNumberOfIntervals();
        return p * numberOfIntervals;
    }

    private static List<Integer> generateRandomChromosomeSetsAndComputeProjectionTest(Genome referenceMap, Genome queryMap, int numberOfExperiments) {

        int capacity = numberOfExperiments;
        List<Integer> stats = IntStream.range(0, capacity).boxed()
                .parallel()
                .map(x -> getProjectionCountForRandomChromosome(referenceMap, queryMap))
                .collect(Collectors.toList());
        return stats;
    }

    private static List<Double> generateRandomChromosomeSetsAndComputeJaccardTest(Genome referenceMap, Genome queryMap, int numberOfExperiments) {
        int capacity = numberOfExperiments;
        List<Double> stats = IntStream.range(0, capacity).boxed()
                .parallel()
                .map(x -> getJaccardTestForRandomChromosome(referenceMap, queryMap))
                .collect(Collectors.toList());
        return stats;
    }

    private static int getProjectionCountForRandomChromosome(Genome reference, Genome query) {
        Genome randomGenome = generateRandomGenomeByReferenceLike(reference, query);
        return ProjectionTest.countProjection(reference, randomGenome);
    }

    private static double getJaccardTestForRandomChromosome(Genome reference, Genome query) {
        Genome randomGenome = generateRandomGenomeByReferenceLike(reference, query);
        return JaccardTest.computeJaccardStatisticForGenome(reference, randomGenome);
    }

    private static Genome generateRandomGenomeByReferenceLike(Genome reference, Genome query) {
        Map<String, Track> referenceMap = new HashMap<>();
        reference.getChromosomes().stream()
                .forEach(x -> referenceMap.put(x.getName(), x.getTrack()));

        Map<String, Track> queryMap = new HashMap<>();
        query.getChromosomes().stream()
                .forEach(x -> queryMap.put(x.getName(), x.getTrack()));

        return RandomTrackGenerator.generateGenomeByReferenceLike(reference, query);
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


    private static Genome readQueryTrackMapFromFile(DataFrameReader dataFrameReader, String path) {

        Dataset<Row> referenceDatasetStartEnd = dataFrameReader
                .load(path)
                .select("chrom", "chromStart", "chromEnd");
        List<Row> rows = referenceDatasetStartEnd.collectAsList();
        Map<String, List<PairStartEnd>> mapStartEnd = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.groupingBy(Interval::getName,
                        Collectors.mapping(x -> new PairStartEnd(x.getStartIndex(), x.getEndIndex()), Collectors.toList())));


        Dataset<Row> queryDataset = dataFrameReader
                .load(path)
                .select("chrom", "chromStart", "chromEnd");

        // TODO remove List<Row> rows and do map() by Spark API?
        rows = queryDataset.collectAsList();
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
        long start;
        long end;

        public PairStartEnd(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

}
