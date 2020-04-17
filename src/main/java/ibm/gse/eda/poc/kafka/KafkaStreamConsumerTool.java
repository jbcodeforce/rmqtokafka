package ibm.gse.eda.poc.kafka;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

public class KafkaStreamConsumerTool {
    public static String TOPIC_NAME = "kafka.topic.name";

    private String topic;
    private String brokers;
    private Properties properties = new Properties();
    private boolean running = true;
    private KafkaConsumer<String, String> kafkaConsumer = null;

    public static String parseArgs(String[] args) {
        int i = 0;
        String arg;
        String propFile = null;
        if (args.length <= 1) {
            System.err.println("Usage KafkaConsumer propertiesfilename | -properties filename");
            System.exit(0);
        }
        while (i < args.length) {
            arg = args[i++];
            if ("-properties".equals(arg)) {
                if (i < args.length)
                    propFile = args[i++];
                else
                    System.err.println("-properties requires a filename");
            } else {
                propFile = arg;
            }
        }
        return propFile;
    }

    public KafkaStreamConsumerTool() {
    }

    public void start() {
       
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.get(StreamsConfig.APPLICATION_ID_CONFIG));
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "rmq-client" + UUID.randomUUID());

        kafkaConsumer = new KafkaConsumer<String, String>(properties);
        kafkaConsumer.subscribe(Collections.singletonList(getTopic()));
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        while (this.running) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(10));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("Consumer Record - key:" + record.key() + " value:" + record.value() + " partition:"
                        + record.partition() + " offset:" + record.offset() + "\n");

            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                close();
            }
        }
        
    }

    public void close(){
        kafkaConsumer.close();
    }

    public void startStream() {
        System.out.println("Start consuming message from " + getTopic());
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> stream = builder
        .stream(
            getTopic()
        );
         stream.peek((k, v) -> System.out.println(k + " --> " + v));
        KafkaStreams streams = new KafkaStreams(builder.build(), properties);
       
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public Properties loadPropertiesFromFile(String propertiesFile) throws IOException{
            Properties props = new Properties();
            props.load(  new FileInputStream(propertiesFile));
            return props;
    }

    private void prepare(Properties config){
        Map<String, String> env = System.getenv();
        properties.putAll(config);
        if (env.containsKey("KAFKA_BROKERS")) {
            setBrokers(env.get("KAFKA_BROKERS"));
        } else {
            setBrokers(config.getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
        }
        if (env.containsKey("KAFKA_TOPIC")) {
            setTopic(env.get("KAFKA_TOPIC"));
        } else {
            setTopic(properties.getProperty(TOPIC_NAME));
        }
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getBrokers());
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.forEach((k, v) -> System.out.println(k + " : " + v));
    
    }
    public static void main(String[] args) {
        String propertiesFileName = parseArgs(args);
        KafkaStreamConsumerTool consumer = new KafkaStreamConsumerTool();
    try {
        consumer.prepare(consumer.loadPropertiesFromFile(propertiesFileName));
        consumer.start();
      } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Properties file not found");

     }
    }


    public String getBrokers() {
        return this.brokers;
    }

    public void setBrokers(String brokers) {
        this.brokers = brokers;
    };

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
