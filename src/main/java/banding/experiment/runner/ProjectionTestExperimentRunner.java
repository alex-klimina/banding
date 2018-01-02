package banding.experiment.runner;

import banding.entity.Genome;
import banding.metric.ProjectionTest;
import banding.report.Report;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectionTestExperimentRunner extends ExperimentRunner {

    public Report getReportForProjectionTest(SparkSession spark, Genome reference, Genome query, int numberOfExperiments) throws IOException {

        Report report = new Report();

        report.setReferenceLength(reference.getLength());
        report.setReferenceCoverage(reference.getCoverage());
        report.setQueryTestValue(ProjectionTest.countProjection(reference, query));

        List<Long> testExperiments =
                generateRandomChromosomeSetsAndComputeTest(reference, query, numberOfExperiments)
                .stream().map(Number::longValue).collect(Collectors.toList());
        report.setTestExperiments(
                testExperiments);

        report.setMean(testExperiments.stream().collect(Collectors.averagingDouble(Double::valueOf)));
        report.setSumDev(testExperiments.stream()
                .map(x -> ((double) x - report.getMean()) * ((double) x - report.getMean()))
                .collect(Collectors.summingDouble(Double::valueOf)));
        report.setSd(Math.sqrt(report.getSumDev() / numberOfExperiments));

        JavaDoubleRDD rdd = getSparkContext(spark).parallelize(testExperiments).mapToDouble(Double::valueOf);
        report.setKolmogorovSmirnovTestResult(
                Statistics.kolmogorovSmirnovTest(rdd, "norm", report.getMean(), report.getSd()));

        TTest tTest = new TTest();
        report.setTTestPValue(tTest.tTest(report.getQueryTestValue(), getDoubleArray(testExperiments)));

        return report;
    }

    protected Number getTestValue(Genome reference, Genome query) {
        return ProjectionTest.countProjection(reference, query);
    }

}
