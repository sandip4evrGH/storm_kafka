package com.test;


import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;

public class StormTopology {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Please provide the Kafka topic name and bootstrap server as command-line arguments.");
            System.exit(1);
        }

        String topic = args[0];  // Topic from the first command-line argument
        String bootstrapServer = args[1];  // Bootstrap server from the second command-line argument

        // Create the topology builder
        TopologyBuilder builder = new TopologyBuilder();

        // Set spout with the specified Kafka topic and bootstrap server
        builder.setSpout("kafka-spout", new KafkaCustomSpout(topic, bootstrapServer), 1);  // Parallelism of 1

        // Set the bolt to process the emitted tuples from the "default" stream
        builder.setBolt("print-bolt", new PrintBolt(), 1).shuffleGrouping("kafka-spout", "default");

        // Configure the topology
        Config config = new Config();
        config.setDebug(true);
        config.setMaxTaskParallelism(3);

        // Create and submit the topology to a local cluster
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Kafka-Topology", config, builder.createTopology());
    }
}



//import org.apache.storm.kafka.spout.KafkaSpout;
//import org.apache.storm.kafka.spout.KafkaSpoutConfig;
//import org.apache.storm.thrift.TException;
//import org.apache.storm.topology.TopologyBuilder;
//import org.apache.storm.Config;
//import org.apache.storm.LocalCluster;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//
//public class StormTopology {
//    public static void main(String[] args) throws TException {
//        if (args.length < 1) {
//            System.err.println("Please provide the Kafka topic name as a command-line argument.");
//            System.exit(1);
//        }
//
//        String topic = args[0];
//
////         Kafka consumer configuration
//        KafkaSpoutConfig<String, String> spoutConfig = KafkaSpoutConfig.builder("localhost:9092", topic) // specify Kafka broker and topic
////                .setGroupId("storm-group") // Set the consumer group ID
//                .setMaxUncommittedOffsets(1000) // Optional: set max uncommitted offsets
//                .setPollTimeoutMs(1000) // Optional: set poll timeout
//                .build();
//
//
//
//        KafkaSpout<String, String> kafkaSpout = new KafkaSpout<>(spoutConfig);
//
//        // Build the topology
//        TopologyBuilder builder = new TopologyBuilder();
//        builder.setSpout("kafka-spout", kafkaSpout, 1);
//        builder.setBolt("print-bolt", new PrintBolt(), 1).shuffleGrouping("kafka-spout");
//
//        // Configuration for Storm
//        Config config = new Config();
//        config.setDebug(true);
//        config.setMaxTaskParallelism(3);
//
//        // Create and submit topology
//        LocalCluster cluster = null;
//        try {
//            cluster = new LocalCluster();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        cluster.submitTopology("Kafka-Topology", config, builder.createTopology());
//    }
//}
