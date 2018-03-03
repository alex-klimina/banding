package banding;

import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.metric.ProjectionTest;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class GraphBuilder {

    private static SparkSession spark = SparkSession
            .builder()
            .master("local[4]")
            .appName("Banding")
            .getOrCreate();

    public static void main(String[] args) {
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

        collect.forEach(x -> System.out.println(x));

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
