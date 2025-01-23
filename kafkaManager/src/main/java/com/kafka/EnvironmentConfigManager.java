package com.kafka;

import picocli.CommandLine;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import picocli.CommandLine.Command;

@Command(name = "env-config-manager", mixinStandardHelpOptions = true, version = "1.0.0", description = "Environment Config Manager")
public class EnvironmentConfigManager implements Runnable {

    @CommandLine.Option(names = {"-env"}, description = "Environment: dev or prod", required = true)
    private String environment;

    @CommandLine.Option(names = {"-command"}, description = "Specify the command to execute (producer/consumer)", required = true)
    private String command;

    @CommandLine.Option(names = {"-kafkaSource"}, description = "Kafka server address", required = true)
    private String kafkaSource;

    @CommandLine.Option(names = {"-topic"}, description = "Kafka topic", required = true)
    private String topic;

    private Properties properties;

    public EnvironmentConfigManager() {
        properties = new Properties();
    }

    private void loadProperties(String environment) throws IOException {
        String fileName = "application-" + environment + ".properties";
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            properties.load(fileInputStream);
        }
    }

    @Override
    public void run() {
        try {
            // Load the corresponding properties file based on the environment
            loadProperties(environment);

            // Retrieve Kafka configuration from the properties file (if needed)
            if (kafkaSource == null) {
                kafkaSource = properties.getProperty("kafka.server");
            }

            if (topic == null) {
                topic = properties.getProperty("kafka.topic");
            }

            // Check if the required properties are available
            if (kafkaSource == null || topic == null) {
                System.err.println("Missing required properties: kafkaSource or topic.");
                return;
            }

            // Run based on the command (producer/consumer)
            if ("producer".equalsIgnoreCase(command)) {
                runProducer(kafkaSource, topic);
            } else if ("consumer".equalsIgnoreCase(command)) {
                runConsumer(kafkaSource, topic);
            } else {
                System.err.println("Invalid command. Use 'producer' or 'consumer'.");
            }

        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
        }
    }

    private void runProducer(String kafkaServer, String topic) {
        // Logic to run Kafka Producer (use kafkaServer and topic)
        System.out.println("Running Kafka Producer with server: " + kafkaServer + " and topic: " + topic);
        // Add Kafka Producer logic here
    }

    private void runConsumer(String kafkaServer, String topic) {
        // Logic to run Kafka Consumer (use kafkaServer and topic)
        System.out.println("Running Kafka Consumer with server: " + kafkaServer + " and topic: " + topic);
        // Add Kafka Consumer logic here
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new EnvironmentConfigManager()).execute(args);
        System.exit(exitCode);
    }
}
