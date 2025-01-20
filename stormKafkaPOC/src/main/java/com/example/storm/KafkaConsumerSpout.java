package com.example.storm;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Collections;
import java.util.Map;

public class KafkaConsumerSpout extends BaseRichSpout {

    private KafkaConsumer<String, String> consumer;
    private SpoutOutputCollector collector;
    private String topic;

    public KafkaConsumerSpout(String topic) {
        this.topic = topic;
    }

//    @Override
//    public void open(Map<String, Object> config, SpoutOutputCollector collector) {
//        this.collector = collector;
//        // Kafka consumer configuration
//        consumer = new KafkaConsumer<>(config);
//        consumer.subscribe(Collections.singletonList(topic));
//    }

    @Override
    public void nextTuple() {
        consumer.poll(100).forEach(record -> {
            // Emit each Kafka record
            collector.emit(new Values(record.key(), record.value()));
        });
    }

    @Override
    public void open(Map<String, Object> map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {

    }

    @Override
    public void close() {
        if (consumer != null) {
            consumer.close();
        }
    }

//    @Override
//    public void declareOutputFields(Fields fields) {
//        fields.declare(new Fields("key", "message"));
//    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
