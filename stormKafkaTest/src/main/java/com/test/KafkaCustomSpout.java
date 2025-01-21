package com.test;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Map;
import java.util.Properties;
import java.util.Collections;
//import org.apache.storm.spout.OutputFieldsDeclarer;

public class KafkaCustomSpout implements IRichSpout {
    private static final Logger logger = LogManager.getLogger(KafkaCustomSpout.class);
    private SpoutOutputCollector collector;
    private Consumer<String, String> consumer;
    private String topic;
    private String bootstrapServer;

    // Constructor that accepts the topic and bootstrap server
    public KafkaCustomSpout(String topic, String bootstrapServer) {
        this.topic = topic;
        this.bootstrapServer = bootstrapServer;
    }

    @Override
    public void open(Map<String, Object> conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;

        // Kafka Consumer setup with user-defined bootstrap server
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);  // Use the passed bootstrap server
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "storm-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Initialize Kafka Consumer
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
    }

    @Override
    public void nextTuple() {
        // Poll for new messages
        ConsumerRecords<String, String> records = consumer.poll(100);

        if (!records.isEmpty()) {
            logger.info("Received " + records.count() + " messages.");

            records.forEach(record -> {
                String key = record.key();
                String value = record.value();
                collector.emit(new Values(key, value)); // Emit records as tuples

                logger.info("Received message: " + key + " - " + value);
            });
        }
    }

    @Override
    public void close() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Override
    public void activate() {
        // Optional: Handle when the spout is activated
    }

    @Override
    public void deactivate() {
        // Optional: Handle when the spout is deactivated
    }

    @Override
    public void ack(Object msgId) {
        // Optional: Handle successful tuple acknowledgment
    }

    @Override
    public void fail(Object msgId) {
        // Optional: Handle failed tuple
    }

//    @Override
//    public Fields getOutputFields() {
//        return new Fields("key", "value");  // Define the output fields for emitted tuples
//    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("key", "value"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;  // Return configuration map if needed
    }
}


