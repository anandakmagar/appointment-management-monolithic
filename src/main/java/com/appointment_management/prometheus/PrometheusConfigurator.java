package com.appointment_management.prometheus;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import com.fasterxml.jackson.databind.ObjectMapper;  // Add this import for JSON parsing

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class PrometheusConfigurator {

    private static final String SECRET_NAME = "GrafanaCloudCredentials";
    private static final Region AWS_REGION = Region.US_EAST_2;
    private static final String PROMETHEUS_CONFIG_PATH = "/etc/prometheus/prometheus.yml";

    public static void main(String[] args) {
        String apiToken = fetchGrafanaApiToken();
        if (apiToken != null) {
            updatePrometheusConfig(apiToken);
            System.out.println("Prometheus configuration updated successfully.");
        } else {
            System.err.println("Failed to fetch Grafana API token.");
        }
    }

    private static String fetchGrafanaApiToken() {
        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(AWS_REGION)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        try {
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(SECRET_NAME)
                    .build();
            GetSecretValueResponse getSecretValueResponse = secretsClient.getSecretValue(getSecretValueRequest);
            String secretString = getSecretValueResponse.secretString();

            // Parse the secret string as JSON and extract the API token
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> secretMap = objectMapper.readValue(secretString, Map.class);
            return secretMap.get("grafana_api_token");
        } catch (SecretsManagerException | IOException e) {
            System.err.println("Error fetching secret from AWS Secrets Manager: " + e.getMessage());
            return null;
        }
    }

    private static void updatePrometheusConfig(String apiToken) {
        try {
            String configContent = new String(Files.readAllBytes(Paths.get(PROMETHEUS_CONFIG_PATH)));
            configContent = configContent.replace("__GRAFANA_API_TOKEN__", apiToken);
            Files.write(Paths.get(PROMETHEUS_CONFIG_PATH), configContent.getBytes());
            System.out.println("Prometheus configuration updated with new API token.");
        } catch (IOException e) {
            System.err.println("Error updating Prometheus configuration: " + e.getMessage());
        }
    }
}
