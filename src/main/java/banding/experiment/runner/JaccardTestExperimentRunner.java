package banding.experiment.runner;

import banding.entity.Genome;
import banding.metric.JaccardTest;
import banding.report.Report;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.mllib.stat.test.KolmogorovSmirnovTestResult;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JaccardTestExperimentRunner extends ExperimentRunner {

    public Report getReportForJaccardTest(SparkSession spark, Genome reference, Genome query, int numberOfExperiments) throws IOException {

        Report report = new Report();

        double queryJaccardTest = JaccardTest.computeJaccardStatisticForGenome(reference, query);

        List<Double> jaccardTestExperiments =
                generateRandomChromosomeSetsAndComputeTest(reference, query, numberOfExperiments)
                .stream().map(Number::doubleValue).collect(Collectors.toList());
        Double mean = jaccardTestExperiments.stream().collect(Collectors.averagingDouble(Double::valueOf));
        Double sumDev = jaccardTestExperiments.stream()
                .map(x -> ((double) x - mean) * ((double) x - mean))
                .collect(Collectors.summingDouble(Double::valueOf));
        double sd = Math.sqrt(sumDev / numberOfExperiments);

        JavaDoubleRDD rdd = getSparkContext(spark).parallelize(jaccardTestExperiments).mapToDouble(Double::doubleValue); //.mapToDouble(Double::valueOf);
        KolmogorovSmirnovTestResult result = Statistics.kolmogorovSmirnovTest(rdd, "norm", mean, sd);

        TTest tTest = new TTest();
        double tTestPValue = tTest.tTest(queryJaccardTest, getDoubleArray(jaccardTestExperiments));

        return report;
    }

    @Override
    protected Number getTestValue(Genome reference, Genome query) {
        return JaccardTest.computeJaccardStatisticForGenome(reference, query);
    }

}
