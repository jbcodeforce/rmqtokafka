#FROM  ibmcom/eventstreams-kafka-ce-icp-linux-amd64:2019.4.1-095ae1e as builder
FROM strimzi/kafka:latest-kafka-2.5.0 as builder
FROM ibmjava:8-jre

RUN addgroup --gid 5000 --system esgroup && \
    adduser --uid 5000 --ingroup esgroup --system esuser

COPY --chown=esuser:esgroup --from=builder /opt/kafka/bin/ /opt/kafka/bin/
COPY --chown=esuser:esgroup --from=builder /opt/kafka/libs/ /opt/kafka/libs/
COPY --chown=esuser:esgroup connectors/db2jcc4.jar /opt/kafka/libs/
COPY --chown=esuser:esgroup --from=builder /opt/kafka/config/connect-distributed.properties /opt/kafka/config/
COPY --chown=esuser:esgroup --from=builder /opt/kafka/config/connect-log4j.properties /opt/kafka/config/
RUN mkdir /opt/kafka/logs && chown esuser:esgroup /opt/kafka/logs

RUN mkdir /opt/connectors && chown esuser:esgroup /opt/connectors
COPY config/ /opt/kafka/config
COPY --chown=esuser:esgroup certs/es-cert.jks /opt/kafka/es-cert.jks 
#COPY --chown=esuser:esgroup certs/registry-es-cert.jks /opt/kafka/registry-es-cert.jks 
COPY --chown=esuser:esgroup connectors /opt/connectors/
# COPY --chown=esuser:esgroup es-cert.jks /opt/kafka/
#set classpath com.ibm.db2.jcc.DB2Driver

WORKDIR /opt/kafka

EXPOSE 8083

USER esuser

ENTRYPOINT ["./bin/connect-distributed.sh", "config/connect-distributed.properties"]
