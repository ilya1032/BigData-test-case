import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.pcap4j.core.PcapNativeException;

import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.concurrent.ExecutionException;

public class Application {
    static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) throws InterruptedException {

        // Create Kafka producer
        Producer<Long, String> kafkaProducer = ProducerCreator.createProducer();

        // Open network interface device handler
        try {
            TrafficHandler.getInstance().openHandle();
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }

        // Initialize traffic limits
        Limits instance = Limits.getInstance();

        // Initiate scheduled traffic limits update every 20 minutes from now
        new Timer().schedule(new UpdateLimitsTask(), 20*60*1000, 20*60*1000);

        // Set spark configuration with 2 threads and fixed application name
        SparkConf sparkConf = new SparkConf().setMaster("local[2]").setAppName("TrafficMonitoringApplication");

        JavaStreamingContext streamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(30));

        // Get size of all received packages and unite them into 5 minute batches every 30 seconds
        JavaDStream<Long> windowedTrafficSizeStream = streamingContext.receiverStream(new TrafficSizeReciever())
                .reduceByWindow(Long::sum, Durations.minutes(5), Durations.seconds(30));

        // For each traffic batch check if traffic size is in limit bounds
        windowedTrafficSizeStream.foreachRDD(longJavaRDD -> {

            for (Long trafficSize : longJavaRDD.collect()) {

                // If not in bounds then send message to Kafka alerts topic
                if (trafficSize < instance.getMinTrafficSize() || trafficSize > instance.getMaxTrafficSize()) {

                    String message = String.format("Alert! Traffic is %s than %d",
                            trafficSize < instance.getMinTrafficSize() ? "less": "more",
                            trafficSize < instance.getMinTrafficSize() ? instance.getMinTrafficSize() : instance.getMaxTrafficSize());
                    ProducerRecord<Long, String> record = new ProducerRecord<>(IKafkaConstants.TOPIC_NAME, message);

                    try {
                        RecordMetadata metadata = kafkaProducer.send(record).get();
                        System.out.println("\n\nRecord sent to partition " + metadata.partition() + " with offset " + metadata.offset() + "\n\n");
                    } catch (ExecutionException | InterruptedException e) {
                        System.out.println("Error sending record!");
                        e.printStackTrace();
                    }
                }
            }
        });

        // Start Spark application and await application termination
        streamingContext.start();
        streamingContext.awaitTermination();

        // Close network device handler
        TrafficHandler.getInstance().closeHandle();
    }
}
