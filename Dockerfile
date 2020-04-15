FROM openjdk:12-alpine
COPY target/rmqtokafka-1.0-SNAPSHOT.jar /home
RUN mkdir /home/config
COPY config /home/config
WORKDIR /home
CMD ["java" "-jar" "rmqtokafka-1.0-SNAPSHOT.jar" "config/kafka.properties"]
