package banding.experiment.runner;

import banding.entity.Genome;
import banding.entity.Track;
import banding.generator.RandomTrackGenerator;
import banding.metric.ProjectionTest;
import banding.report.Report;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProjectionTestExperimentRunner {

    private Report getReportForProjectionTest(SparkSession spark, Genome reference, Genome query, int numberOfExperiments) throws IOException {

        Report report = new Report();

        report.setReferenceLength(reference.getLength());
        report.setReferenceCoverage(reference.getCoverage());
        report.setQueryProjectionTest(ProjectionTest.countProjection(reference, query));

        List<Long> projectionTestExperiments = generateRandomChromosomeSetsAndComputeProjectionTest(reference, query, numberOfExperiments);
        report.setProjectionTestExperiments(
                projectionTestExperiments);

        report.setMean(projectionTestExperiments.stream().collect(Collectors.averagingDouble(Double::valueOf)));
        report.setSumDev(projectionTestExperiments.stream()
                .map(x -> ((double) x - report.getMean()) * ((double) x - report.getMean()))
                .collect(Collectors.summingDouble(Double::valueOf)));
        report.setSd(Math.sqrt(report.getSumDev() / numberOfExperiments));

        JavaDoubleRDD rdd = getSparkContext(spark).parallelize(projectionTestExperiments).mapToDouble(Double::valueOf);
        report.setKolmogorovSmirnovTestResult(
                Statistics.kolmogorovSmirnovTest(rdd, "norm", report.getMean(), report.getSd()));

        TTest tTest = new TTest();
        report.setTTestPValue(tTest.tTest(report.getQueryProjectionTest(), getDoubleArray(projectionTestExperiments)));

        return report;
    }

    private static List<Long> generateRandomChromosomeSetsAndComputeProjectionTest(Genome referenceMap, Genome queryMap, int numberOfExperiments) {

        int capacity = numberOfExperiments;
        List<Long> stats = IntStream.range(0, capacity).boxed()
                .parallel()
                .map(x -> getProjectionCountForRandomChromosome(referenceMap, queryMap))
                .collect(Collectors.toList());
        return stats;
    }

    private static long getProjectionCountForRandomChromosome(Genome reference, Genome query) {
        Genome randomGenome = generateRandomGenomeByReferenceLike(reference, query);
        return ProjectionTest.countProjection(reference, randomGenome);
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


    private double[] getDoubleArray(List<? extends Number> list) {
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i).doubleValue();
        }
        return arr;
    }

    private JavaSparkContext getSparkContext(SparkSession spark) {
        return JavaSparkContext.fromSparkContext(spark.sparkContext());
    }
}
