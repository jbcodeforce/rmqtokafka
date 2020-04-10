# Rabbit MQ to Kafka using IBM messaging connector

## Pre-requisites

* Get Rabbitmq java client library

## Summary of MQRabbit concepts

* Connection abstracts the socket connection, and takes care of protocol version negotiation and authentication
* Channel support the operation on the queue, like publish
* Acknowledgements are from consumers to RabbitMQ: When RabbitMQ delivers a message to a consumer, it needs to know when to consider the message to be successfully sent. 
* Publisher confirms are from broker ack to publisher.
* Broker sends message with a delivery tag, scoped per channel. Delivery tags, is a monotonically growing positive number sent by producer the consumer uses for acknowledgement.
* Acknowledged message as delivered, can be discarded from the queue.
* Automatic ack can be done, a fire and forget model, producer does not wait to remove message from queue. If the socket fails, data is lost.
* Manual acknowledgements can be batched to reduce network traffic.
* Sometimes a consumer cannot process a delivery immediately but other instances might be able to. In this case it may be desired to requeue it and let another consumer receive and handle it.
* Consumer implementations can track the number of redeliveries and reject messages for good (discard them) or schedule requeueing after a delay.
* As producing is asynchronous, multiple 'in flight' messages are in a channel at a given point of time. There is a sliding window of delivery tags that are unacknowledged. (QoS prefetch)
* The flow of deliveries and manual client acknowledgements is entirely asynchronous.

## Tests

### Basic write to Rabbit MQ queue

The java code is based on AMQP.

```java
ConnectionFactory factory = new ConnectionFactory();
factory.setHost(hostname);
Gson parser = new Gson();
try (Connection connection = factory.newConnection();
    Channel channel = connection.createChannel()) {
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        Customer c = new Customer("U01", "Bob", "The Builder","12-04-2000");
        String message = parser.toJson(c);
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
```

Start it using maven: `mvn exec:java -Dexec.mainClass=ibm.gse.eda.AmqpSender`

### Basic get message from queue

```java
hostname = (String)System.getenv().getOrDefault("MQHOST","localhost");
ConnectionFactory factory = new ConnectionFactory();
factory.setHost(hostname);
Gson parser = new Gson();
Connection connection = factory.newConnection();
Channel channel = connection.createChannel();
channel.queueDeclare(QUEUE_NAME, false, false, false, null);
System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

DeliverCallback deliverCallback = (consumerTag, delivery) -> {
    String message = new String(delivery.getBody(), "UTF-8");
    System.out.println(" [x] Received '" + message + "'");
};
channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
```

Start it using maven: `mvn exec:java -Dexec.mainClass=ibm.gse.eda.AmqpReceiver`

### Tools

/opt/rabbitmq/sbin/rabbitmqctl list_queues

### Things to address when implementing a consumer

* What is the input throughput that may be difficult to process by consumer generating buffering and event back preassure implementation.
* Do we have multiple consumers sharing a queue? This could generate concurrency race condition and requeued message could be in different position, instead of its original position.
* Finding a suitable prefetch value is a matter of trial and error and will vary from workload to workload. Values in the 100 through 300 range usually offer optimal throughput and do not run significant risk of overwhelming consumers



