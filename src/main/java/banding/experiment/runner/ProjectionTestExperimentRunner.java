package banding.experiment.runner;

import banding.entity.Genome;
import banding.metric.ProjectionTest;

public class ProjectionTestExperimentRunner extends ExperimentRunner {

    protected Number getTestValue(Genome reference, Genome query) {
        return ProjectionTest.countProjection(reference, query);
    }

}
