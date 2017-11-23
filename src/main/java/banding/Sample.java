package banding;


import banding.entity.Interval;
import banding.metric.JaccardTest;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.col;

public class Sample {

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

        Dataset<Row> dataset = dataFrameReader
                .load("src/main/resources/hgTables_ref.csv")
                .filter(col("chrom").equalTo("chr1"))
                .filter(col("gieStain").equalTo("gpos100").or(col("gieStain").equalTo("gpos75")))
                .select("chrom", "chromStart", "chromEnd", "gieStain");

        List<Row> rows = dataset.collectAsList();
        Deque<Interval> intervalRef = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.toCollection(ArrayDeque::new));

        dataset = dataFrameReader
                .load("src/main/resources/hgTables_ref.csv")
                .filter(col("chrom").equalTo("chr1"))
                .select("chrom", "chromStart", "chromEnd", "gieStain");

        rows = dataset.collectAsList();
        Deque<Interval> intervalCpG = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.toCollection(ArrayDeque::new));


        double jaccardStatistic = JaccardTest.computeJaccardStatisticForChromosome(intervalCpG, intervalRef);
        System.out.println(jaccardStatistic);


        System.out.println("===== test ");

        dataset = dataFrameReader
                .load("src/test/resources/ref.txt")
//                .filter(col("chrom").equalTo("chr1"))
                .select("chrom", "chromStart", "chromEnd");
        dataset.show();

        rows = dataset.collectAsList();
        intervalRef = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.toCollection(ArrayDeque::new));

        dataset = dataFrameReader
                .load("src/test/resources/query.txt")
//                .filter(col("chrom").equalTo("chr1"))
                .select("chrom", "chromStart", "chromEnd");
        dataset.show();

        rows = dataset.collectAsList();
        Deque<Interval> intervalQuery = rows.stream()
                .map(r -> new Interval(r.getString(0), r.getInt(1), r.getInt(2)))
                .collect(Collectors.toCollection(ArrayDeque::new));


        jaccardStatistic = JaccardTest.computeJaccardStatisticForChromosome(intervalQuery, intervalRef);
        System.out.println(jaccardStatistic);

    }
}
