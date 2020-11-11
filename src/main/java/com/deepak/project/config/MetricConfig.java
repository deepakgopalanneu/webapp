package com.deepak.project.config;

import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricConfig {

   @Value("true")
    private boolean publish;

   @Value("localhost")
    private String host;

   @Value("8125")
    private int port;

   @Value("csye6225")
    private String prefix;

   @Bean
    public StatsDClient metricsClient() {
        if(publish){
            return new NonBlockingStatsDClient(prefix, host, port);
        }
        return new NoOpStatsDClient();
    }



}