package com.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.errors.ThrottlingQuotaExceededException;

import java.util.Properties;

public class KafkaProducerService {

    private String kafkaServer;
    private String topic;
    private boolean isAvro;

    public KafkaProducerService(String kafkaServer, String topic, boolean isAvro) {
        this.kafkaServer = kafkaServer;
        this.topic = topic;
        this.isAvro = isAvro;
    }

    public void sendMessages(String message) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", isAvro ? "io.confluent.kafka.serializers.KafkaAvroSerializer" : StringSerializer.class.getName());

        KafkaProducer<String, Object> producer = new KafkaProducer<>(props);

        try {
            producer.send(new ProducerRecord<>(topic, "key", message));
            producer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }

    // Handle sending Avro records
    public void sendMessagesFromAvroRecord(GenericRecord avroRecord) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");

        KafkaProducer<String, Object> producer = new KafkaProducer<>(props);

        try {
            producer.send(new ProducerRecord<>(topic, "key", avroRecord));
            producer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }

    public void printTotalMessagesSent() {
        System.out.println("Total messages sent to topic: " + topic);
    }

    public void close() {
        // Close any resources if needed
    }
}
