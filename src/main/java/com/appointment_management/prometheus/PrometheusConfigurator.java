package com.appointment_management.prometheus;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PrometheusConfigurator {

    private static final String SECRET_NAME = "GrafanaCloudCredential";
    private static final Region AWS_REGION = Region.US_EAST_2; // Adjust the region as necessary
    private static final String PROMETHEUS_CONFIG_PATH = "src/main/resources/prometheus.yml";

    public static void main(String[] args) {
        String apiToken = fetchGrafanaApiToken();
        if (apiToken != null) {
            System.out.println("Fetched API Token: " + apiToken); // Debugging: Log the fetched token
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
            System.out.println("Attempting to fetch secret from AWS Secrets Manager..."); // Debugging: Log before fetching
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(SECRET_NAME)
                    .build();
            GetSecretValueResponse getSecretValueResponse = secretsClient.getSecretValue(getSecretValueRequest);
            String secretString = getSecretValueResponse.secretString();
            System.out.println("Secret fetched successfully: " + secretString); // Debugging: Log the raw secret string

            // In this case, since you're storing the secret as plaintext, simply return the secret string
            return secretString;
        } catch (SecretsManagerException e) {
            System.err.println("Error fetching secret from AWS Secrets Manager: " + e.awsErrorDetails().errorMessage());
            e.printStackTrace(); // Debugging: Print stack trace for detailed error
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
            e.printStackTrace(); // Debugging: Print stack trace for detailed error
        }
    }
}
