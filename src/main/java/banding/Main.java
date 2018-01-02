package banding;

import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.entity.Track;
import banding.experiment.runner.ProjectionTestExperimentRunner;
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

        List<String> queryPaths = new ArrayList<>();
        queryPaths.add("src/main/resources/hgTables_CpG.csv");
        queryPaths.add("src/main/resources/hgTables_DNAse_HS.csv");
        queryPaths.add("src/main/resources/hgTables_layered_H3K4Me1.csv");
        queryPaths.add("src/main/resources/hgTables_microsatellit.csv");

        for (String queryPath: queryPaths) {
            Genome query = readQueryTrackMapFromFile(dataFrameReader, queryPath);
            String outputProjectionTest = "reportProjectionTest_" + queryPath + "_.txt";
            ProjectionTestExperimentRunner.getReportForProjectionTest(spark, reference, query, 1000);
            String outputJaccardTest = "reportJaccardTest_" + queryPath + "_.txt";
            main.getReportForJaccardTest(reference, query, outputJaccardTest);
        }
    }

    private void getReportForJaccardTest(Genome reference, Genome query, String output) throws IOException {
        File file = new File(output);
        addLineToFile(file, "Сoverage of reference: " + reference.getCoverage());
        addLineToFile(file, "Length of reference: " + reference.getLength());

        double queryProjectionTest = JaccardTest.computeJaccardStatisticForGenome(reference, query);
        addLineToFile(file, "JaccardTest for query: "
                + queryProjectionTest);

        int n = 1000;
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

    private static double getExpectedValueForBinomialDistribution(Genome reference, Genome query) {
        double p = ((double) reference.getCoverage()) / ( (double) reference.getLength());
        long numberOfIntervals = query.getNumberOfIntervals();
        return p * numberOfIntervals;
    }

    private static List<Long> generateRandomChromosomeSetsAndComputeProjectionTest(Genome referenceMap, Genome queryMap, int numberOfExperiments) {

        int capacity = numberOfExperiments;
        List<Long> stats = IntStream.range(0, capacity).boxed()
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

    private static long getProjectionCountForRandomChromosome(Genome reference, Genome query) {
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
