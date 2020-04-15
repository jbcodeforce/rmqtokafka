package ibm.gse.eda.poc.rmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;


/**
 * Receive message from a queue using AMQP api
 *
 */
public class AmqpReceiver {

    private static String hostname;
    private static String queueName;
    private Channel channel;
    private Connection connection;
    private ConnectionFactory factory;

    public AmqpReceiver() {
        hostname = (String) System.getenv().getOrDefault(CommonConfig.RMQ_HOST, "localhost");
        queueName = (String) System.getenv().getOrDefault(CommonConfig.RMQ_QUEUE_NAME, "accounts");
        factory = new ConnectionFactory();
        factory.setHost(hostname);
    }

    public AmqpReceiver(String hname, String qname) {
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
    
    public DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [x] Received '" + message + "'");
    };

    public void poll() throws IOException {
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    public static void main( String[] args ) throws Exception
    {
        System.out.println( "Hello rabbitmq  consumer!" );
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        AmqpReceiver receiver = new AmqpReceiver();
        receiver.connectToQueue();
        receiver.poll();
               
    }
}
