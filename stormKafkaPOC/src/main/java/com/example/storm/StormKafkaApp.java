package com.example.storm;

import org.apache.storm.LocalCluster;
import org.apache.storm.Config;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.generated.StormTopology;

public class StormKafkaApp {

    public static void main(String[] args) throws Exception {
        // Load Spring configuration
        ApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");

        // Retrieve topology bean from Spring context
        KafkaStormTopology topologyBean = (KafkaStormTopology) context.getBean("topology");

        // Build the Storm topology
        StormTopology topology = topologyBean.buildTopology();

        // Set up a local cluster and submit the topology
        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        config.setDebug(true);
        cluster.submitTopology("KafkaStormTopology", config, topology);
    }
}
