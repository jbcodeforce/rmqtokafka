package ibm.gse.eda.poc.rmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import ibm.gse.eda.poc.domain.Customer;

/**
 * Send message to a queue using AMQP api
 *
 */
public class AmqpSender {

    private static String queueName;
    private static String hostname;
    private Channel channel;
    private Connection connection;
    private ConnectionFactory factory;

    public AmqpSender() {
        hostname = (String) System.getenv().getOrDefault(CommonConfig.RMQ_HOST, "localhost");
        queueName = (String) System.getenv().getOrDefault(CommonConfig.RMQ_QUEUE_NAME, "accounts");
        factory = new ConnectionFactory();
        factory.setHost(hostname);
    }

    public AmqpSender(String hname, String qname) {
        hostname = hname;
        queueName = qname;
        factory = new ConnectionFactory();
        factory.setHost(hostname);
    }

    public void connectToQueue() throws IOException, TimeoutException {
        connectToQueue(queueName);
    }

    public void connectToQueue(String qname) throws IOException, TimeoutException {
        connection = factory.newConnection();
        channel = connection.createChannel();
        queueName = qname;
        channel.queueDeclare(qname, false, false, false, null);
    }

    public static void main(String[] args) throws Exception {
        int nbRecords = 10;
        if (args.length != 0) {
            nbRecords= Integer.parseInt(args[0]);
        }
        AmqpSender sender = new AmqpSender();
        Gson parser = new Gson();
        for (int i = 0; i < nbRecords ;i++){
            Customer c = new Customer("U0"+i, "Bob", "The Builder", "12-04-2000");
            String messageToSend = parser.toJson(c);
            sender.connectToQueue();
            sender.publish(messageToSend);
        }
       
    }

    public void publish(String messageToSend) throws IOException {
        this.channel.basicPublish("", queueName, null, messageToSend.getBytes());
        System.out.println(" [x] Sent '" + messageToSend + "'");
	}
}
