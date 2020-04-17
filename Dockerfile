FROM openjdk:12-alpine
COPY target/rmqtokafka-1.0-SNAPSHOT-jar-with-dependencies.jar /home
RUN mkdir /home/config
COPY config /home/config
WORKDIR /home
CMD java -jar rmqtokafka-1.0-SNAPSHOT-jar-with-dependencies.jar -properties config/kafka.properties
