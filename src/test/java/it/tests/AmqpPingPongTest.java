package it.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;

import org.junit.Assert;
import org.junit.Test;

import ibm.gse.eda.AmqpSender;
import ibm.gse.eda.poc.domain.Customer;

/**
 * Send a message to a queue and get the message back
 */
public class AmqpPingPongTest {
    AmqpSender sender = new AmqpSender();
    Gson parser = new Gson();

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldGetMessageSent() {
        Customer c = new Customer("U01", "Bob", "The Builder", "12-04-2000");
        String messageToSend = parser.toJson(c);
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
        assertTrue( true );
    }
}
