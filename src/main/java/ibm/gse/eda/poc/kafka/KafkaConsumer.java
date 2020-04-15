package ibm.gse.eda.poc.kafka;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

public class KafkaConsumer {
    public static String TOPIC_NAME = "kafka.topic.name";

    private String topic;
    private String brokers;
    private Properties properties = new Properties();

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



   

    public KafkaConsumer() {
    }

    public void start() {
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> stream = builder
        .stream(
            (String)properties.get(TOPIC_NAME)
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
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getBrokers());
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.forEach((k, v) -> System.out.println(k + " : " + v));
    
    }
    public static void main(String[] args) {
        String propertiesFileName = parseArgs(args);
        KafkaConsumer consumer = new KafkaConsumer();
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
