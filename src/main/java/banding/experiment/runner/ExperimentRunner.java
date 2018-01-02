package banding.experiment.runner;

import banding.entity.Genome;
import banding.entity.Track;
import banding.generator.RandomTrackGenerator;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class ExperimentRunner {

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
