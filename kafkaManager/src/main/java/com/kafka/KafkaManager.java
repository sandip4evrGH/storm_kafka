package com.kafka;

import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(name = "kafka-manager", mixinStandardHelpOptions = true, version = "1.0.0", description = "Kafka Manager CLI")
public class KafkaManager implements Runnable {

    @CommandLine.Option(names = "-kafkaSource", description = "Kafka server address", required = true)
    private String kafkaServer;

    @CommandLine.Option(names = "-topic", description = "Kafka topic", required = true)
    private String topic;

    @CommandLine.Option(names = "-produceFilePath", description = "File path for producing messages")
    private String produceFilePath;

    @CommandLine.Option(names = "-consumeFilePath", description = "File path for consuming messages")
    private String consumeFilePath;

    @CommandLine.Option(names = "-avro", description = "Enable Avro serialization/deserialization", defaultValue = "false")
    private boolean isAvro;

    @CommandLine.Option(names = "-command", description = "The command to run (producer, consumer, copy)", required = true)
    private String command;

    @CommandLine.Option(names = "-sourceTopic", description = "Source Kafka topic for copying")
    private String sourceTopic;

    @CommandLine.Option(names = "-destinationTopic", description = "Destination Kafka topic for copying")
    private String destinationTopic;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new KafkaManager()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // Validate the file paths for producing or consuming messages
        if (produceFilePath != null) {
            if (!isValidFilePath(produceFilePath)) {
                System.err.println("Error: The file " + produceFilePath + " does not exist or is not a valid file.");
                return;
            }
        }

        if (consumeFilePath != null) {
            if (!isValidFilePath(consumeFilePath)) {
                System.err.println("Error: The file " + consumeFilePath + " does not exist or is not a valid file.");
                return;
            }
        }

        // Run the selected command based on provided arguments
        if (sourceTopic != null && destinationTopic != null) {
            runCopy();  // Run the topic copy logic
        } else if (produceFilePath != null) {
            runProducer();  // Run the producer logic
        } else if (consumeFilePath != null) {
            runConsumer();  // Run the consumer logic
        } else {
            System.err.println("Error: Invalid command or missing required file path.");
        }
    }

    private void runProducer() {
        KafkaProducerService producerService = new KafkaProducerService(kafkaServer, topic, isAvro);
        producerService.sendMessages(produceFilePath);
        producerService.printTotalMessagesSent();
        producerService.close();
    }

    private void runConsumer() {
        KafkaConsumerService consumerService = new KafkaConsumerService(kafkaServer, topic, isAvro);
        consumerService.consumeMessages();
        consumerService.printTotalMessagesConsumed();
        consumerService.close();
    }

    private void runCopy() {
        if (sourceTopic == null || destinationTopic == null) {
            System.out.println("Please provide both source and destination topics for copying.");
            return;
        }

        KafkaConsumerService copyConsumerService = new KafkaConsumerService(kafkaServer, sourceTopic, isAvro);
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

    // Helper method to check if the file exists and is a valid file
    private boolean isValidFilePath(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

}
