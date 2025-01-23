package com.kafka;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.*;
import java.util.Properties;

public class KafkaProducerService {

    private String kafkaServer;
    private String topic;
    private boolean isAvro;
    private KafkaProducer<String, Object> producer;

    public KafkaProducerService(String kafkaServer, String topic, boolean isAvro) {
        this.kafkaServer = kafkaServer;
        this.topic = topic;
        this.isAvro = isAvro;

        // Initialize Kafka producer in constructor
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", isAvro ? "io.confluent.kafka.serializers.KafkaAvroSerializer" : StringSerializer.class.getName());

        this.producer = new KafkaProducer<>(props);
    }

    public void sendMessages(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    if (isAvro) {
                        GenericRecord avroRecord = createAvroRecord(line);
                        producer.send(new ProducerRecord<>(topic, "key", avroRecord));
                    } else {
                        producer.send(new ProducerRecord<>(topic, "key", line));
                    }
                    producer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GenericRecord createAvroRecord(String message) {
        // Implement your Avro serialization logic here.
        return null;
    }

    // Close the producer to release resources
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }

    // Method to send Avro records
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
}
