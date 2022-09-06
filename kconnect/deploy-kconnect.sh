#clean up
rm -rf k8s
rm -rf connectors
mkdir connectors
cd connectors
#pull jdbc jar from artifactory
curl https://artifactory-tools.tch-cluster-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-south.containers.appdomain.cloud/artifactory/libs-snapshot-local/com/ibm/eventstreams/connect/kafka-connect-rabbitmq-source/1.0-SNAPSHOT/kafka-connect-rabbitmq-source-1.0-20200513.190436-1-jar-with-dependencies.jar -O
#pull avro converter
curl https://artifactory-tools.tch-cluster-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-south.containers.appdomain.cloud/artifactory/libs-snapshot-local/com/ibm/eventstreams/connect/kafka-connect-avro-converter/1.0-SNAPSHOT/kafka-connect-avro-converter-1.0-20200513.182234-1-jar-with-dependencies.jar -O
cd ..
docker build -t kconnect .
Docker tag  kconnect default-route-openshift-image-registry.tch-cluster-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-south.containers.appdomain.cloud/rabbitmq/kconnect$1

Docker push default-route-openshift-image-registry.tch-cluster-0143c5dd31acd8e030a1d6e0ab1380e3-0000.us-south.containers.appdomain.cloud/rabbitmq/kconnect$1

# create a temporary directory for deployment
mkdir deploy$1
yq w k8s/deployment-rmq-kconnect.yaml "spec.template.spec.containers[0].image" image-registry.openshift-image-registry.svc:5000/rabbitmq/kconnect$1 > deploy$1/deployment-rmq-kconnect.yaml
yq w -i deploy$1/deployment-rmq-kconnect.yaml "metadata.name" rmq-connect$1 
yq w -i deploy$1/deployment-rmq-kconnect.yaml "spec.selector.matchLabels.app" rmq-connect$1 
yq w -i deploy$1/deployment-rmq-kconnect.yaml "spec.template.metadata.labels.app" rmq-connect$1 
yq w -i deploy$1/deployment-rmq-kconnect.yaml "spec.template.spec.containers[0].name" rmq-connect$1 

yq w k8s/service-rmq-kconnect.yaml "metadata.name" rmq-connect$1 > deploy$1/service-rmq-kconnect.yaml
yq w -i deploy$1/service-rmq-kconnect.yaml "spec.selector.app" rmq-connect$1 
oc delete -f deploy$1
oc delete route/rmq-connect$1
oc create -f deploy$1
oc expose svc/rmq-connect$1 -n rabbitmq
oc get -n rabbitmq route,svc,pods


