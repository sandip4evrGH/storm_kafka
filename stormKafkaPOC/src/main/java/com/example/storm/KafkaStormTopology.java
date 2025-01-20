package com.example.storm;

import org.apache.storm.Config;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class KafkaStormTopology {

    private String topic;

    public KafkaStormTopology(String topic) {
        this.topic = topic;
    }

    public StormTopology buildTopology() {
        TopologyBuilder builder = new TopologyBuilder();

        // Define the Kafka spout as a source
        builder.setSpout("kafkaSpout", new KafkaConsumerSpout(topic), 1);

        // Define a simple bolt to process messages from Kafka
        builder.setBolt("printBolt", new PrintBolt(), 1).shuffleGrouping("kafkaSpout");

        return builder.createTopology();
    }

    public static class PrintBolt extends BaseRichBolt {

        @Override
        public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {

        }

        @Override
        public void execute(org.apache.storm.tuple.Tuple input) {
            String key = input.getStringByField("key");
            String message = input.getStringByField("message");
            System.out.println("Received message: " + message + " with key: " + key);
        }


        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            // Declare the fields that this bolt emits (key and message)
            declarer.declare(new Fields("key", "message"));
        }
    }
}
