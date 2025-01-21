package com.test;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.topology.OutputFieldsDeclarer;

import java.util.Map;

public class PrintBolt implements IRichBolt {

    private OutputCollector collector;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;  // Save the collector for emitting tuples
    }

    @Override
    public void execute(Tuple input) {
        // Print the incoming message
        String message = input.getStringByField("value");
        System.out.println("Received message: " + message);

        // Acknowledge the tuple after processing
        collector.ack(input);
    }

    @Override
    public void cleanup() {
        // Cleanup resources
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // No output fields in this example
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}

