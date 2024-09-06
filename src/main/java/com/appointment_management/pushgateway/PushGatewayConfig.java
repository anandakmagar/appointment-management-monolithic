package com.appointment_management.pushgateway;


import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class PushGatewayConfig {

    @Bean
    public PushGateway pushGateway() {
        return new PushGateway("http://ec2-3-137-156-96.us-east-2.compute.amazonaws.com:9191");
    }

    public void pushMetrics(PrometheusMeterRegistry prometheusMeterRegistry, PushGateway pushGateway) {
        // Extract the Prometheus CollectorRegistry from PrometheusMeterRegistry
        CollectorRegistry registry = prometheusMeterRegistry.getPrometheusRegistry();

        try {
            // Push metrics to PushGateway
            pushGateway.pushAdd(registry, "my-heroku-app");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




