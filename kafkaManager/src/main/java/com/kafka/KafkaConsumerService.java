package com.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.avro.generic.GenericRecord;

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

    public KafkaConsumerService(String kafkaServer, String topic, String consumerGroupId, boolean isAvro) {
        this.kafkaServer = kafkaServer;
        this.topic = topic;
        this.consumerGroupId = consumerGroupId;
        this.isAvro = isAvro;
    }

    // Consumes messages from the Kafka topic and saves them to a ZIP file
    public void consumeMessagesAndSaveToZip(String zipFilePath) throws IOException {
        // Set Kafka consumer properties
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("group.id", consumerGroupId);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", isAvro ? "io.confluent.kafka.serializers.KafkaAvroDeserializer" : StringDeserializer.class.getName());

        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        // Create a ZIP file output stream
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            int messageCount = 0;

            while (messageCount < 100) {  // You can adjust this as needed
                ConsumerRecords<String, Object> records = consumer.poll(1000);  // Poll for messages (1-second timeout)

                for (ConsumerRecord<String, Object> record : records) {
                    String consumedMessage;

                    if (isAvro) {
                        // Deserialize Avro message
                        GenericRecord avroRecord = (GenericRecord) record.value();
                        consumedMessage = avroRecord.toString();  // Convert Avro record to string (you can adjust this based on your Avro schema)
                    } else {
                        // Handle string messages
                        consumedMessage = record.value().toString();
                    }

                    // Write each consumed message to the ZIP file
                    ZipEntry entry = new ZipEntry("consumed_message_" + messageCount + ".txt");
                    zos.putNextEntry(entry);
                    zos.write(consumedMessage.getBytes());
                    zos.closeEntry();

                    messageCount++;  // Increment message count
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to ZIP file: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error during Kafka consumption: " + e.getMessage());
            throw new RuntimeException("Error consuming messages from Kafka", e);
        } finally {
            consumer.close();  // Close the Kafka consumer when done
        }
    }

    public void consumeMessages() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("group.id", consumerGroupId);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", isAvro ? "io.confluent.kafka.serializers.KafkaAvroDeserializer" : StringDeserializer.class.getName());

        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        try {
            while (true) {
                ConsumerRecords<String, Object> records = consumer.poll(1000);  // Poll for messages (1-second timeout)
                for (ConsumerRecord<String, Object> record : records) {
                    if (isAvro) {
                        // Deserialize Avro
                        GenericRecord avroRecord = (GenericRecord) record.value();
                        // Handle the Avro record (you'll need to use your own Avro schema)
                    } else {
                        // Handle string messages
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

    // New method to consume messages from one topic and forward to another topic
    public void consumeMessagesAndCopy(KafkaProducerService producerService) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("group.id", "consumerGroupId");
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
