package com.kafka;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.*;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Command(name = "kafka-manager", mixinStandardHelpOptions = true, version = "1.0.0", description = "Kafka Manager CLI")
public class KafkaManager implements Runnable {

    @CommandLine.Option(names = "-config", description = "Path to the property file (e.g., dev01.properties, prod2.properties)", required = true)
    private String configFilePath;

    @CommandLine.Option(names = "-command", description = "The command to run (producer, consumer, copy)", required = true)
    private String command;

    @CommandLine.Option(names = "-isSaveFile", description = "Whether to save consumed messages to a ZIP file", defaultValue = "false")
    private boolean isSaveFile;

    private String kafkaServer;
    private String consumerGroupId;
    private String topic;
    private String produceFilePath;
    private boolean isAvro;
    private String sourceTopic;
    private String destinationTopic;
    private String saveFilePath;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new KafkaManager()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // Load properties from the specified configuration file
        Properties properties = loadProperties(configFilePath);

        if (properties == null) {
            System.err.println("Error: Unable to load properties from file " + configFilePath);
            return;
        }

        // Set values from the properties file
        kafkaServer = properties.getProperty("kafka.server");
        consumerGroupId = properties.getProperty("kafka.consumer.group.id");
        topic = properties.getProperty("kafka.topic");
        produceFilePath = properties.getProperty("produceFilePath");
        isAvro = Boolean.parseBoolean(properties.getProperty("avro", "false"));
        sourceTopic = properties.getProperty("sourceTopic");
        destinationTopic = properties.getProperty("destinationTopic");
        saveFilePath = properties.getProperty("saveFilePath"); // Path where the ZIP file will be saved (if applicable)

        // Validate required properties for both producer and consumer
        if (kafkaServer == null || topic == null || consumerGroupId == null) {
            System.err.println("Error: Missing required properties (kafka.server, kafka.topic, or kafka.consumer.group.id).");
            return;
        }

        // Run the selected command based on the provided command-line option
        switch (command.toLowerCase()) {
            case "producer":
                if (produceFilePath == null) {
                    System.err.println("Error: Missing produceFilePath for producer.");
                    return;
                }
                // Validate the file path for producing messages
                if (!isValidFilePath(produceFilePath)) {
                    System.err.println("Error: The file " + produceFilePath + " does not exist or is not a valid file.");
                    return;
                }
                runProducer();
                break;
            case "consumer":
                runConsumer();
                break;
            case "copy":
                if (sourceTopic != null && destinationTopic != null) {
                    runCopy();  // Run the topic copy logic
                } else {
                    System.err.println("Error: Missing source or destination topic for copy.");
                }
                break;
            default:
                System.err.println("Error: Invalid command. Please use 'producer', 'consumer', or 'copy'.");
                break;
        }
    }

    private void runProducer() {
        KafkaProducerService producerService = new KafkaProducerService(kafkaServer, topic, isAvro);
        producerService.sendMessages(produceFilePath);
        producerService.printTotalMessagesSent();
        producerService.close();
    }

    private void runConsumer() {
        KafkaConsumerService consumerService = new KafkaConsumerService(kafkaServer, topic, consumerGroupId, isAvro);

        // Consume messages and optionally save to a file
        if (isSaveFile && saveFilePath != null) {
            try {
                consumerService.consumeMessagesAndSaveToZip(saveFilePath);
            } catch (IOException e) {
                System.err.println("Error saving consumed messages to ZIP: " + e.getMessage());
            }
        } else {
            consumerService.consumeMessages();
        }

        consumerService.printTotalMessagesConsumed();
        consumerService.close();
    }

    private void runCopy() {
        KafkaConsumerService copyConsumerService = new KafkaConsumerService(kafkaServer, sourceTopic, consumerGroupId, isAvro);
        KafkaProducerService copyProducerService = new KafkaProducerService(kafkaServer, destinationTopic, isAvro);

        // Consume from source topic and produce to destination topic
        copyConsumerService.consumeMessagesAndCopy(copyProducerService);

        // Print the total number of events processed
        copyConsumerService.printTotalMessagesConsumed();
        copyProducerService.printTotalMessagesSent();

        // Close both consumer and producer
        copyConsumerService.close();
        copyProducerService.close();
    }

    // Helper method to load properties from a file
    private Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + e.getMessage());
            return null;
        }
        return properties;
    }

    // Helper method to check if the file exists and is a valid file
    private boolean isValidFilePath(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

}
