package com.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.Schema;
import org.apache.kafka.common.errors.ThrottlingQuotaExceededException;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.specific.SpecificDatumWriter;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import java.io.ByteArrayOutputStream;
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
            // If Avro, serialize the message into Avro format; otherwise send it as a plain string
            if (isAvro) {
                // If the message is Avro, convert it to Avro before sending
                GenericRecord avroRecord = createAvroRecord(message); // Assume we have a method to create the Avro record
                producer.send(new ProducerRecord<>(topic, "key", avroRecord));
            } else {
                producer.send(new ProducerRecord<>(topic, "key", message));
            }
            producer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }

    // This method simulates creating an Avro record based on a message
    private GenericRecord createAvroRecord(String message) {
        try {
            // Example schema, you would normally load it from a file or schema registry
            String schemaStr = "{\n" +
                    "  \"type\": \"record\",\n" +
                    "  \"name\": \"MessageRecord\",\n" +
                    "  \"fields\": [\n" +
                    "    {\"name\": \"message\", \"type\": \"string\"}\n" +
                    "  ]\n" +
                    "}";

            Schema schema = new Schema.Parser().parse(schemaStr);
            GenericRecord record = new org.apache.avro.generic.GenericData.Record(schema);
            record.put("message", message);

            return record;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    // Serialize the Avro record manually (if needed)
    public byte[] serializeAvro(GenericRecord avroRecord) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DatumWriter<GenericRecord> datumWriter = new SpecificDatumWriter<>(avroRecord.getSchema());
        Encoder encoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);
        datumWriter.write(avroRecord, encoder);
        encoder.flush();
        return byteArrayOutputStream.toByteArray();
    }

    public void printTotalMessagesSent() {
        System.out.println("Total messages sent to topic: " + topic);
    }

    public void close() {
        // Close any resources if needed
    }
}
