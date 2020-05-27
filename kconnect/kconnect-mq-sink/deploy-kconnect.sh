#clean up
rm -rf k8s
rm -rf connectors
mkdir connectors
cd connectors
#pull rabbitmq-connector and avro converter jar from artifactory
#curl https://artifactory-tools.tch-cluster-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-south.containers.appdomain.cloud/artifactory/libs-snapshot-local/com/ibm/eventstreams/connect/kafka-connect-mq-sink/1.3.0-SNAPSHOT/kafka-connect-mq-sink-1.3.0-20200519.001159-1-jar-with-dependencies.jar -O
#pull avro converter
curl https://artifactory-tools.tch-cluster-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-south.containers.appdomain.cloud/artifactory/libs-snapshot-local/com/ibm/eventstreams/connect/kafka-connect-avro-converter/1.0-SNAPSHOT/kafka-connect-avro-converter-1.0-20200513.182234-1-jar-with-dependencies.jar -O
cd ..
cp -f *.jar connectors
Docker build -t mq-sink-connector$1 . 
Docker tag  mq-sink-connector$1 default-route-openshift-image-registry.tch-cluster-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-south.containers.appdomain.cloud/rabbitmq/mq-sink-connector$1

Docker push default-route-openshift-image-registry.tch-cluster-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-south.containers.appdomain.cloud/rabbitmq/mq-sink-connector$1

# create a temporary directory for deployment
mkdir deploy$1
mkdir k8s
yq w ../k8s/deployment-rmq-kconnect.yaml "spec.template.spec.containers[0].image" image-registry.openshift-image-registry.svc:5000/rabbitmq/mq-sink-connector$1 > k8s/deployment-rmq-mq-sink-connector.yaml
yq w -i k8s/deployment-rmq-mq-sink-connector.yaml "metadata.name" mq-sink-connector$1 
yq w -i k8s/deployment-rmq-mq-sink-connector.yaml "spec.selector.matchLabels.app" mq-sink-connector$1 
yq w -i k8s/deployment-rmq-mq-sink-connector.yaml "spec.template.metadata.labels.app" mq-sink-connector$1 
yq w -i k8s/deployment-rmq-mq-sink-connector.yaml "spec.template.spec.containers[0].name" mq-sink-connector$1 

yq w ../k8s/service-rmq-kconnect.yaml "metadata.name" mq-sink-connector$1 > k8s/service-rmq-mq-sink-connector.yaml
yq w -i k8s/service-rmq-mq-sink-connector.yaml "spec.selector.app" mq-sink-connector$1 
oc delete -f k8s
oc delete route/mq-sink-connector$1
oc create -f k8s
oc expose svc/mq-sink-connector$1 -n rabbitmq
oc get -n rabbitmq route,svc,pods


