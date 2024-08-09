package com.appointment_management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PrometheusService {

    private final WebClient webClient;
    private final String prometheusUrl;
    private final String username;
    private final String apiToken;

    public PrometheusService(WebClient webClient,
                             @Value("${prometheus.url}") String prometheusUrl,
                             @Value("${prometheus.username}") String username) {
        this.webClient = webClient;
        this.prometheusUrl = prometheusUrl;
        this.username = username;
        this.apiToken = System.getenv("GRAFANA_API_TOKEN");
    }

    public void pushMetrics(String metricsData) {
        webClient.post()
                .uri(prometheusUrl + "/api/prom/push")
                .headers(headers -> headers.setBasicAuth(username, apiToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(metricsData)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> System.out.println("Response from Prometheus: " + response))
                .doOnError(error -> System.err.println("Error occurred: " + error.getMessage()))
                .subscribe();
    }
}
