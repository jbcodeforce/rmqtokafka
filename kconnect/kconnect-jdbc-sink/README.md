##
to run the deploy-kconnect.sh, pass the number of the connector you want to deploy.
For example, ./deploy-kconnect.sh 1, will deploy jdbc-sink-connector1

Make sure you've created the topics in the connect-distributed.properties file:
offset.storage.topic=connect-offsets-jdbc-1
offset.storage.replication.factor=25

config.storage.topic=connect-configs-jdbc-1
config.storage.replication.factor=1

status.storage.topic=connect-status-jdbc-1
status.storage.replication.factor=5