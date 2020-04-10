package ibm.gse.eda;

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
        hostname = (String) System.getenv().getOrDefault("MQHOST", "localhost");
        queueName = (String) System.getenv().getOrDefault("QUEUE_NAME", "testqueue");
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
        AmqpSender sender = new AmqpSender();
        Gson parser = new Gson();
        Customer c = new Customer("U01", "Bob", "The Builder", "12-04-2000");
        String messageToSend = parser.toJson(c);
        sender.connectToQueue();
        sender.publish(messageToSend);
    }

    public void publish(String messageToSend) throws IOException {
        this.channel.basicPublish("", queueName, null, messageToSend.getBytes());
        System.out.println(" [x] Sent '" + messageToSend + "'");
	}
}
