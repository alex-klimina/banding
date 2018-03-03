package banding;

import banding.entity.Chromosome;
import banding.entity.Genome;
import banding.entity.Interval;
import banding.metric.ProjectionTest;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        int numberOfSplittedInterval = 1000;
//        int numberOfSplittedInterval = 10;
        Interval chr1_1 = reference.getChromosome("chr1").getTrack().getIntervals().getFirst();
        long length = chr1_1.getLength();
        long lengthOfSubIntervals = length / numberOfSplittedInterval;


        Chromosome queryChromosome = query.getChromosome("chr1");
        long countProjection = ProjectionTest.countProjection(chr1_1, queryChromosome);
        System.out.println("for whole band: " + countProjection);

        List<Interval> splittedIntervals = IntStream.range(0, numberOfSplittedInterval).boxed()
                .map(x -> new Interval( chr1_1.getStartIndex() + x * lengthOfSubIntervals,
                                        chr1_1.getStartIndex() + (x + 1) * lengthOfSubIntervals - 1))
                .collect(Collectors.toList());

        List<Long> values = splittedIntervals.stream()
                .map(x -> ProjectionTest.countProjection(x, queryChromosome))
                .collect(Collectors.toList());

        System.out.println("sourced band: " + chr1_1);
//        System.out.println("Splitted intervals: " + splittedIntervals);
//        System.out.println("for splitted band: " + values);
        System.out.println("Sum for splitted: " + values.stream().collect(Collectors.summingLong(Long::valueOf)));

    }



}
