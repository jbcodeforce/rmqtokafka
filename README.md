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

## RabbitMQ to Kafka using IBM Event Streams connector

This section highlights the instructions to deploy a custom RabbitMQ to event streams kafka connector. The following first 3 steps are already done and the rabbitmq connector jar file is already under the `kconnect/connectors` folder.

1. Clone the IBM Rabbitmq connector repository: [https://github.com/ibm-messaging/kafka-connect-rabbitmq-source](https://github.com/ibm-messaging/kafka-connect-rabbitmq-source)
1. Build the jar file of this connector with the dependencies, under the kafka-connect-rabbitmq-source folder using: `mvn clean package`
1. Copy the generated "with-dependencies" jar file under `kconnect/connectors` folder of this repository. 
1. Define / review the configuration properties for the connector: `config/connect-distributed.properties`. For example verify the connect topic names and kafka bootstrap server URL.
1. Download the CA certificate (truststore packaging) to access Event Streams deployed on CP4I under the `certs` folder. The file is named `es-cert.jks`.
1. Package the kafka connector with your configuration: `docker build -t ibmcase/rmqk .` The dockerfile is in the kconnect folder and use the /opt/connectors and config folder content.
1. Create the expected topics on the Target Kafka cluster:

    ```shell
    ./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic connect-offsets
    ./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic connect-status 
    ./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic connect-configs
    ```
1. Push the created image to a docker registry, private or docker hub: `docker push ibmcase/rmqk`
1. Connect via `oc login...` command to the OpenShift cluster
1. Define the deployment and the service for the connector in OCP. The configuration files are under the `k8s` folder. You may want to change the name, namespace...

    ```shell
    oc apply -f deployment-rmq-kconnect.yaml
    oc apply -f service-rmq-kconnect.yaml
    ```
1. Verify the connector pod is up and running: `oc get pods`
1. Expose the service with a route: `oc expose service rmq-kconnect`. Then get the URL of the connector: `oc describe route rmq-kconnect-route`
1. Tune the json descriptor for the connector definition: As the Connector runs in distributed mode, we can add RabbitMQ configuration by posting to the url of the Connector: `rmq-kconnect-route-rmq.tchcluster-cp4i-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-south.containers.appdomain.cloud/connectors`. An example of json is part of the connector source and reproduced here for the example we used:

    ```json
      "name": "RabbitMQSourceConnector",
    "config": {
        "connector.class": "com.ibm.eventstreams.connect.rabbitmqsource.RabbitMQSourceConnector",
        "tasks.max": "2",
        "kafka.topic" : "accounts",
        "rabbitmq.queue" : "accounts",
        "rabbitmq.host" : "rabbitmq",
        "rabbitmq.prefetch.count" : "500",
        "rabbitmq.automatic.recovery.enabled" : "true",
        "rabbitmq.network.recovery.interval.ms" : "10000",
        "rabbitmq.topology.recovery.enabled" : "true"
    }
    ```

1. Run a Rabbit MQ message producer to write message to the `accounts` queue. In this project we use the AmqpSender class. As an alternate you can use the RabbitMq console.
1. Run a Kafka consumer on the target topic.


If you run the local Kafka cluster and RabbitMQ docker containers via docker compose, your can run the connector with the following command: `docker run --network kafkanet -p8083:8083 ibmcase/rmqk`