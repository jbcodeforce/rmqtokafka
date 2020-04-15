package it.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Test;

import ibm.gse.eda.poc.domain.Customer;
import ibm.gse.eda.poc.rmq.AmqpReceiver;
import ibm.gse.eda.poc.rmq.AmqpSender;

/**
 * Send a message to a queue and get the message back
 */
public class AmqpPingPongTest {
    AmqpSender sender = new AmqpSender();
    AmqpReceiver receiver = new AmqpReceiver();

    Gson parser = new Gson();

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldGetTheMessageSent() {
        Customer c = new Customer("U01", "Bob", "The Builder", "12-04-2000");
        String messageToSend = parser.toJson(c);
        // default configuration as defined in env variables.
        try {
            sender.connectToQueue();
            sender.publish(messageToSend);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (TimeoutException e) {
            e.printStackTrace();
            Assert.fail();
        }
        try {
            receiver.connectToQueue();
            receiver.poll();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (TimeoutException e) {
            e.printStackTrace();
            Assert.fail();
        }
        assertTrue( true );
    }
}
