package com.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.avro.generic.GenericRecord;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerService {

    private String kafkaServer;
    private String topic;
    private boolean isAvro;

    public KafkaConsumerService(String kafkaServer, String topic, boolean isAvro) {
        this.kafkaServer = kafkaServer;
        this.topic = topic;
        this.isAvro = isAvro;
    }

    public void consumeMessages() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("group.id", "kafka-manager");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", isAvro ? "io.confluent.kafka.serializers.KafkaAvroDeserializer" : StringDeserializer.class.getName());

        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        try {
            while (true) {
                ConsumerRecords<String, Object> records = consumer.poll(1000);
                for (ConsumerRecord<String, Object> record : records) {
                    if (isAvro) {
                        // Deserialize Avro
                        GenericRecord avroRecord = (GenericRecord) record.value();
                        // Handle the Avro record (you'll need to use your own Avro schema)
                    } else {
                        // Handle the string message
                        System.out.println("Consumed message: " + record.value());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    // New method to consume from one topic and forward to another topic
    public void consumeMessagesAndCopy(KafkaProducerService producerService) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("group.id", "kafka-manager");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", isAvro ? "io.confluent.kafka.serializers.KafkaAvroDeserializer" : StringDeserializer.class.getName());

        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        try {
            while (true) {
                ConsumerRecords<String, Object> records = consumer.poll(1000);
                for (ConsumerRecord<String, Object> record : records) {
                    if (isAvro) {
                        // Deserialize Avro
                        GenericRecord avroRecord = (GenericRecord) record.value();
                        // Forward the Avro record to the producer
                        producerService.sendMessagesFromAvroRecord(avroRecord);
                    } else {
                        // Forward the string message to the producer
                        producerService.sendMessages(record.value().toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    public void printTotalMessagesConsumed() {
        System.out.println("Total messages consumed from topic: " + topic);
    }

    public void close() {
        // Close any resources if needed
    }
}
