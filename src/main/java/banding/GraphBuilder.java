package banding;

import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.metric.ProjectionTest;
import org.apache.commons.io.FileUtils;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.SparkSession;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class GraphBuilder {

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

        String referencePath = args[0];
        Genome reference = Main.readReferenceTrackMapFromFile(dataFrameReader, referencePath);

        String queryPath = args[1];
        Genome query = Main.readQueryTrackMapFromFile(dataFrameReader, queryPath);

        List<List<Long>> collect = reference.getChromosomes().stream()
                .flatMap(s -> getMetricValueForChromosome(reference, query, s.getName()).stream())
                .collect(toList());


        String outTable = collect.stream()
                .map(band -> band.stream()
                        .map(x -> x.toString())
                        .collect(Collectors.joining(",")))
                .collect(Collectors.joining("\n"));

        FileUtils.write(new File("outTable"), outTable);


        List<Double> valueForGraph = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            int tempvar = i;
            Double average_i = collect.stream()
                    .map(list -> list.get(tempvar))
                    .collect(Collectors.averagingLong(Long::valueOf));
            valueForGraph.add(i, average_i);
        }

        String stringForGraph = valueForGraph.stream()
                .map(x -> x.toString())
                .collect(joining(","));
        FileUtils.write(new File("dataForGraph"), stringForGraph);


    }

    static List<List<Long>> getMetricValueForChromosome(Genome reference, Genome query, String chromosomeName) {
        Chromosome queryChromosome = query.getChromosome(chromosomeName);
        return reference.getChromosome(chromosomeName).getTrack().getIntervals().stream()
                .map(referenceBand -> getValuesForBand(referenceBand, queryChromosome))
                .collect(toList());
    }

    static List<Long> getValuesForBand(Interval band, Chromosome queryChromosome) {
        int numberOfSplittedInterval = 1000;
        long length = band.getLength();
        long lengthOfSubIntervals = length / numberOfSplittedInterval;

        List<Interval> splittedIntervals = IntStream.range(0, numberOfSplittedInterval).boxed()
                .map(x -> new Interval(band.getStartIndex() + x * lengthOfSubIntervals,
                        band.getStartIndex() + (x + 1) * lengthOfSubIntervals - 1))
                .collect(toList());

        return splittedIntervals.stream()
                .map(x -> ProjectionTest.countProjection(x, queryChromosome))
                .collect(toList());
    }

}
