

public class Sample {

    public static void main(String[] args) {

        SparkSession spark = SparkSession
                .builder()
                .appName("JavaSparkPi")
                .getOrCreate();

    }
}
