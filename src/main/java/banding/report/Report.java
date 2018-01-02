package banding.report;

import lombok.Data;
import org.apache.spark.mllib.stat.test.KolmogorovSmirnovTestResult;

import java.util.List;

@Data
public class Report {

    private String testName;
    private long referenceLength;
    private long referenceCoverage;
    private Number queryTestValue;
    private List<? extends Number> testExperiments;
    private Double mean;
    private Double sumDev;
    private double sd;
    private KolmogorovSmirnovTestResult kolmogorovSmirnovTestResult;
    private double tTestPValue;

}
