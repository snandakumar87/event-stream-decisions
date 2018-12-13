/* kafka-spark-openshift-java

This is a skeleton application for processing stream data from Apache
Kafka with Apache Spark. It will read messages on an input topic and
simply echo those message to the output topic.

This application uses Spark's _Structured Streaming_ interface, for
more information please see
https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html
*/
package io.radanalytics.eventstreamdecisions;

import java.util.*;

import com.Event;
import com.TrainingModel;
import com.eventAnalysis;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;

public class App {
    public static void main(String[] args) throws Exception {
        String brokers = System.getenv("KAFKA_BROKERS");
        String intopic = System.getenv("KAFKA_IN_TOPIC");
        String outtopic = System.getenv("KAFKA_OUT_TOPIC");

        if (brokers == null) {
            System.out.println("KAFKA_BROKERS must be defined.");
            System.exit(1);
        }
        if (intopic == null) {
            System.out.println("KAFKA_IN_TOPIC must be defined.");
            System.exit(1);
        }

        if (outtopic == null) {
            System.out.println("KAFKA_OUT_TOPIC must be defined.");
            System.exit(1);
        }

        StructType event_msg_struct = new StructType()
            .add("event_id", DataTypes.StringType)
            .add("event_category", DataTypes.StringType)
            .add("event_value", DataTypes.StringType)
            .add("event_src", DataTypes.StringType)
            ;

        /* acquire a SparkSession object */
        SparkSession spark = SparkSession
            .builder()
            .appName("KafkaSparkOpenShiftJava")
            .getOrCreate();

        /* setup rules processing */

        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
        KieBase rules = loadRules();
        Broadcast<KieBase> broadcastRules = sc.broadcast(rules);

        /* register a user defined function to apply rules on events */
        spark.udf().register("eventfunc", (String eventId, String eventCategory, String eventValue, String eventSrc) -> {
            KieSession session = broadcastRules.value().newKieSession();
            System.out.println("created session");
            Event e = new Event();
            e.setEventId(eventId);
            e.setEventCategory(eventCategory);
            e.setEventValue(eventValue);
            e.setEventSource(eventSrc);
            e.setEventDate(new Date());
            session.insert(e);


            System.out.println("inserted event in session");
            TrainingModel trainingModel2 = new TrainingModel("CUSTOMER_GOOD_STANDING",100);
            session.insert(trainingModel2);
            System.out.println("inserted training model in session");
            int i = session.fireAllRules();
            if(i > 0) {
            eventAnalysis eventAnalys = null;

            Collection<?> objects = session.getObjects(new ClassObjectFilter(eventAnalysis.class));
 
            eventAnalys = (eventAnalysis) objects.iterator().next();
            
            return eventAnalys.toString(); } else return "Values"+e.getEventCategory() + e.getEventValue();
        }, DataTypes.StringType);

        /* configure the operations to read the input topic */
        Dataset<Row> records = spark
            .readStream()
            .format("kafka")
            .option("kafka.bootstrap.servers", brokers)
            .option("subscribe", intopic)
            .load()
            .select(functions.column("value").cast(DataTypes.StringType).alias("value"))
            .select(functions.from_json(functions.column("value"), event_msg_struct).alias("json"))
            .select(functions.callUDF("eventfunc",
                                     functions.column("json.event_id"),
                                     functions.column("json.event_category"),
                                     functions.column("json.event_value"),
                                     functions.column("json.event_src")).alias("value"));

        /* configure the output stream */
        StreamingQuery writer = records
            .writeStream()
            .format("kafka")
            .option("kafka.bootstrap.servers", brokers)
            .option("topic", outtopic)
            .option("checkpointLocation", "/tmp")
            .start();

        /* begin processing the input and output topics */
        writer.awaitTermination();
    }

    public static KieBase loadRules() {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        return kieContainer.getKieBase();
    }

}
