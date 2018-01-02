package banding;

import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.entity.Track;
import banding.experiment.runner.JaccardTestExperimentRunner;
import banding.experiment.runner.ProjectionTestExperimentRunner;
import banding.report.Report;
import org.apache.commons.io.FileUtils;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.col;

public class Main {

    private static SparkSession spark = SparkSession
            .builder()
            .master("local[4]")
            .appName("Banding")
            .getOrCreate();

    public static void main(String[] args) throws IOException {

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

        int numberOfExperiments = 10;

        for (String queryPath: queryPaths) {
            Genome query = readQueryTrackMapFromFile(dataFrameReader, queryPath);

            String outputProjectionTest = "reportProjectionTest_" + queryPath + "_.txt";
            Report reportForProjectionTest = new ProjectionTestExperimentRunner()
                    .getReportForTest(spark, reference, query, numberOfExperiments);
            FileUtils.write(new File(outputProjectionTest), reportForProjectionTest.toString());

            String outputJaccardTest = "reportJaccardTest_" + queryPath + "_.txt";
            Report reportForJaccardTest = new JaccardTestExperimentRunner()
                    .getReportForTest(spark, reference, query, numberOfExperiments);
            FileUtils.write(new File(outputJaccardTest), reportForJaccardTest.toString());
        }
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
