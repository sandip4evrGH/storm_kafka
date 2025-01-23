package com.kafka;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.*;
import java.util.Collections;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class KafkaConsumerService {

    private String kafkaServer;
    private String topic;
    private String consumerGroupId;
    private boolean isAvro;
    private KafkaConsumer<String, Object> consumer;

    public KafkaConsumerService(String kafkaServer, String topic, String consumerGroupId, boolean isAvro) {
        this.kafkaServer = kafkaServer;
        this.topic = topic;
        this.consumerGroupId = consumerGroupId;
        this.isAvro = isAvro;

        // Initialize Kafka consumer in constructor
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("group.id", consumerGroupId);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", isAvro ? "io.confluent.kafka.serializers.KafkaAvroDeserializer" : StringDeserializer.class.getName());

        this.consumer = new KafkaConsumer<>(props);
    }

    public void consumeMessages() {
        consumer.subscribe(Collections.singletonList(topic));

        try {
            while (true) {
                ConsumerRecords<String, Object> records = consumer.poll(1000);
                for (ConsumerRecord<String, Object> record : records) {
                    if (isAvro) {
                        GenericRecord avroRecord = (GenericRecord) record.value();
                        // Handle Avro record (process or save)
                    } else {
                        System.out.println("Consumed message: " + record.value());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void consumeMessagesAndSaveToZip(String zipFilePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            ZipEntry entry = new ZipEntry("consumed_messages.txt");
            zos.putNextEntry(entry);

            // Example: consume some messages and write to ZIP file
            String message = "Sample Kafka message";
            zos.write(message.getBytes());
            zos.closeEntry();
        }
    }
    // New method to consume messages and forward them to another topic using the producer service
    public void consumeMessagesAndCopy(KafkaProducerService producerService) {
        consumer.subscribe(Collections.singletonList(topic));

        try {
            while (true) {
                ConsumerRecords<String, Object> records = consumer.poll(1000);
                for (ConsumerRecord<String, Object> record : records) {
                    if (isAvro) {
                        GenericRecord avroRecord = (GenericRecord) record.value();
                        producerService.sendMessagesFromAvroRecord(avroRecord);
                    } else {
                        producerService.sendMessages(record.value().toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Close the consumer to release resources
    public void close() {
        if (consumer != null) {
            consumer.close();
        }
    }
}
