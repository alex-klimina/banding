package banding.experiment.runner;

import banding.entity.Genome;
import banding.entity.Track;
import banding.generator.RandomTrackGenerator;
import banding.report.Report;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.mllib.stat.test.KolmogorovSmirnovTestResult;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class ExperimentRunner {

    public Report getReportForTest(SparkSession spark, Genome reference, Genome query, int numberOfExperiments) {

        List<? extends Number> testExperiments =
                getTestExperiments(reference, query, numberOfExperiments);

        Report report = new Report();
        report.setTestName(getTestName());
        report.setReferenceLength(reference.getLength());
        report.setReferenceCoverage(reference.getCoverage());
        report.setQueryTestValue(getTestValue(reference, query));
        report.setTestExperiments(testExperiments);
        report.setMean(getMean(testExperiments));
        report.setSumDev(getSumOfDeviation(testExperiments));
        report.setSd(getStandardDeviation(testExperiments));
        report.setKolmogorovSmirnovTestResult(getKolmogorovSmirnovTestResult(spark, report, testExperiments));
        report.setTTestPValue(getTTestPValue(report, testExperiments));

        return report;
    }

    private Double getMean(List<? extends Number> testExperiments) {
        return testExperiments.stream()
                .map(Number::doubleValue)
                .collect(Collectors.averagingDouble(Double::valueOf));
    }

    private Double getSumOfDeviation(List<? extends Number> testExperiments) {
        Double mean = getMean(testExperiments);
        return testExperiments.stream()
                .map(Number::doubleValue)
                .map(x -> (x - mean) * (x - mean))
                .collect(Collectors.summingDouble(Double::valueOf));
    }

    private Double getStandardDeviation(List<? extends Number> testExperiments) {
        Double sumOfDeviation = getSumOfDeviation(testExperiments);
        return Math.sqrt(sumOfDeviation / testExperiments.size());
    }

    private KolmogorovSmirnovTestResult getKolmogorovSmirnovTestResult(SparkSession spark, Report report, List<? extends Number> testExperiments) {
        JavaDoubleRDD rdd = getSparkContext(spark)
                .parallelize(testExperiments)
                .mapToDouble(Number::doubleValue);
        return Statistics.kolmogorovSmirnovTest(rdd, "norm", report.getMean(), report.getSd());
    }

    private double getTTestPValue(Report report, List<? extends Number> testExperiments) {
        TTest tTest = new TTest();
        return tTest.tTest(report.getQueryTestValue().doubleValue(), getDoubleArray(testExperiments));
    }

    private List<Long> getTestExperiments(Genome reference, Genome query, int numberOfExperiments) {
        return generateRandomChromosomeSetsAndComputeTest(reference, query, numberOfExperiments)
                .stream().map(Number::longValue).collect(Collectors.toList());
    }

    protected abstract String getTestName();

    protected List<Number> generateRandomChromosomeSetsAndComputeTest(Genome referenceMap, Genome queryMap, int numberOfExperiments) {

        int capacity = numberOfExperiments;
        List<Number> stats = IntStream.range(0, capacity).boxed()
                .parallel()
                .map(x -> getTestForRandomChromosome(referenceMap, queryMap))
                .collect(Collectors.toList());
        return stats;
    }

    protected Number getTestForRandomChromosome(Genome reference, Genome query) {
        Genome randomGenome = generateRandomGenomeByReferenceLike(reference, query);
        return getTestValue(reference, randomGenome);
    }

    protected abstract Number getTestValue(Genome reference, Genome query);

    protected static Genome generateRandomGenomeByReferenceLike(Genome reference, Genome query) {
        Map<String, Track> referenceMap = new HashMap<>();
        reference.getChromosomes().stream()
                .forEach(x -> referenceMap.put(x.getName(), x.getTrack()));

        Map<String, Track> queryMap = new HashMap<>();
        query.getChromosomes().stream()
                .forEach(x -> queryMap.put(x.getName(), x.getTrack()));

        return RandomTrackGenerator.generateGenomeByReferenceLike(reference, query);
    }

    protected static double[] getDoubleArray(List<? extends Number> list) {
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i).doubleValue();
        }
        return arr;
    }

    protected static JavaSparkContext getSparkContext(SparkSession spark) {
        return JavaSparkContext.fromSparkContext(spark.sparkContext());
    }
}
