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

    @Override
    protected String getTestName() {
        return "Jaccard Test";
    }

    @Override
    protected Number getTestValue(Genome reference, Genome query) {
        return JaccardTest.computeJaccardStatisticForGenome(reference, query);
    }

}
