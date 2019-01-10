# event stream decisions


This demo shows how we can define a guided decision table in Red Hat decision manager and get the kjar imported as dependency inside of a spark application. The spark job reads a customer profile (in json format) with real time events and a set of training models which can be fed in from within a big data analysis engine (using AI/ML). The job when complete outputs the CustomerProfile with useful events which can be sent to downstream systems for taking action. The events are filtered by passing them through a guided decision table which analyses real time events against training models analyzed over a historic time period.

Refer to https://github.com/snandakumar87/PersonalizationRules for the rules project. The project can be imported on Red Hat Decision Manager by importing the Repository link. Since the Spark Application code needs the mvn dependency for the rule, this has build and deployed so that it is available in the local maven repo. For the purpose of testing, i have added a static version of the rule mvn jar in the repo.

a project for processing an event stream using business rules

the subdirectories in this repository are applications that comprise this
demonstration. the emitter will produce messages that are broadcast onto an
apache kafka topic. the processor will use apache spark to read messages from
the topic and then use drools to apply rules to each message. the results of
the rules application is then broadcast onto a second topic.

# quickstart

**prerequisites**

* an openshift cluster available.

* a terminal with the openshift command line client and active login
  session to the openshift cluster available.

**proceedure**

1. create a new project for kafka. a separate project is used for kafka to
   help isolate its components.
   ```
   oc new-project kafka
   ```

1. install strimzi.io kafka templates into the new project. this step is
   using the 0.1.0 release as it is lightweight and can be installed with
   minimal openshift privileges.
   ```
   oc create -f https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.1.0/kafka-inmemory/resources/openshift-template.yaml
   ```

1. start apache kafka. this will launch the necessary components to start
   kafka, it may take a few minutes to download all the images and start them.
   ```
   oc new-app strimzi
   ```

1. create a new project for the event-stream-decisions application. this
   helps to isolate the applications from the kafka deployment, if you prefer
   you can use the same project for all components.
   ```
   oc new-project events
   ```

1. install the radanalytics.io templates into the project. these templates
   are used to deploy apache spark along with the emitter-processor service.
   ```
   oc create -f https://radanalytics.io/resources.yaml
   ```

1. deploy the event-emitter. this service will generate the event data to be
   processed.
   ```
   oc new-app centos/python-36-centos7~https://github.com/snandakumar87/event-stream-decisions \
     --context-dir event-emitter \
     -e KAFKA_BROKERS=kafka.kafka.svc:9092 \
     -e KAFKA_TOPIC=events \
     -e RATE=1 \
     --name=emitter
   ```

1. deploy the event-processor. this service will utilize apache spark and
   drools to process the event data. it may take a few minutes for the images
   to download and deploy, this will create a spark cluster in addition to
   the processor service.
   ```
   oc new-app --template oshinko-java-spark-build-dc \
       -p APPLICATION_NAME=event-processor \
       -p GIT_URI=https://github.com/snandakumar87/event-stream-decisions \
       -p CONTEXT_DIR=event-processor \
       -p APP_FILE=eventstreamdecisions-1.0-SNAPSHOT.jar \
       -p SPARK_OPTIONS='--packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --conf spark.jars.ivy=/tmp/.ivy2' \
       -e KAFKA_BROKERS=kafka.kafka.svc:9092 \
       -e KAFKA_IN_TOPIC=events \
       -e KAFKA_OUT_TOPIC=responses
   ```

after all the components have deployed you will have the application pipeline
running and broadcasting results onto the `responses` topic.

**monitoring**

to watch the events as they are processed you can use this
[simple listener service](https://github.com/bones-brigade/kafka-openshift-python-listener)
by running the following commands.

1. deploy the listener.
   ```
   oc new-app centos/python-36-centos7~https://github.com/bones-brigade/kafka-openshift-python-listener.git \
     -e KAFKA_BROKERS=kafka.kafka.svc:9092 \
     -e KAFKA_TOPIC=responses \
     --name=listener
   ```

1. follow the logs to see the results.
   ```
   oc logs -f dc/listener
   ```
