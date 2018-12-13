# event processor

A Java source-to-image application for processing events from the data-emitter
using business rules.

## Launch on OpenShift

### Prerequisites

1. radanalytics installed in your OpenShift project. see the
   [radanalytics.io Get Started page](https://radanalytics.io/get-started)
   for instructions on installing that tooling.

### Proceedure

1. launch the app with the following command:
   ```bash
   oc new-app --template oshinko-java-spark-build-dc \
       -p APPLICATION_NAME=event-processor \
       -p GIT_URI=https://github.com/elmiko/event-stream-decisions \
       -p CONTEXT_DIR=event-processor \
       -p APP_FILE=eventstreamdecisions-1.0-SNAPSHOT.jar \
       -p SPARK_OPTIONS='--packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --conf spark.jars.ivy=/tmp/.ivy2' \
       -e KAFKA_BROKERS=kafka:9092 \
       -e KAFKA_IN_TOPIC=topic1 \
       -e KAFKA_OUT_TOPIC=topic2
   ```

You will need to adjust the `KAFKA_BROKERS`, `KAFKA_IN_TOPIC`, and
`KAFKA_OUT_TOPIC` to match your Kafka configuration and desired topics.  In
this example, our application will subscribe to messages on the Kafka topic
`topic1`, and it will publish messages on the topic `topic2` using the broker
at `apache-kafka:9092`.
