package banding.experiment.runner;

import banding.entity.Genome;
import banding.metric.ProjectionTest;

public class ProjectionTestExperimentRunner extends ExperimentRunner {

    @Override
    protected String getTestName() {
        return "Projection test";
    }

    protected Number getTestValue(Genome reference, Genome query) {
        return ProjectionTest.countProjection(reference, query);
    }

}
