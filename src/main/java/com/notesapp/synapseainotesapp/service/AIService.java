package com.notesapp.synapseainotesapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    private static final String SUMMARIZATION_API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";
    private static final String ZERO_SHOT_CLASSIFICATION_API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";

    public AIService() {
        this.restTemplate = new RestTemplate();
    }

    public String summarizeText(String textToSummarize) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.replace("Bearer ", ""));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", textToSummarize);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("min_length", 50);
        parameters.put("max_length", 250);
        payload.put("parameters", parameters);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<List<Map<String, String>>> response = restTemplate.exchange(
                    SUMMARIZATION_API_URL,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, String>>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isEmpty()) {
                return response.getBody().get(0).get("summary_text");
            } else {
                System.err.println("Error from Hugging Face API (Summarization): " + response.getStatusCode());
                System.err.println("Response body: " + response.getBody());
                return "Error: Could not summarize text. Status: " + response.getStatusCode();
            }
        } catch (Exception e) {
            System.err.println("Exception during API call to Hugging Face (Summarization): " + e.getMessage());
            e.printStackTrace();
            return "Error: Exception occurred while summarizing text.";
        }
    }

    public String suggestCategory(String textToCategorize, List<String> candidateLabels) {
        if (candidateLabels == null || candidateLabels.isEmpty()) {
            System.err.println("Candidate labels cannot be null or empty for category suggestion.");
            return "Error: No candidate labels provided for categorization.";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.replace("Bearer ", ""));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", textToCategorize);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("candidate_labels", candidateLabels);
        payload.put("parameters", parameters);



        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    ZERO_SHOT_CLASSIFICATION_API_URL,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("labels") && responseBody.containsKey("scores")) {
                    List<String> labels = (List<String>) responseBody.get("labels");
                    List<Double> scores = (List<Double>) responseBody.get("scores");

                    if (!labels.isEmpty() && !scores.isEmpty()) {
                        // The API usually returns labels sorted by score (highest first)
                        return labels.get(0);
                    } else {
                        System.err.println("Hugging Face API (Categorization) returned empty labels or scores.");
                        return "Error: Could not determine category (empty labels/scores).";
                    }
                } else {
                    System.err.println("Hugging Face API (Categorization) response missing 'labels' or 'scores' field.");
                    System.err.println("Response body: " + responseBody);
                    return "Error: Could not determine category (invalid response structure).";
                }
            } else {
                System.err.println("Error from Hugging Face API (Categorization): " + response.getStatusCode());
                System.err.println("Response body: " + response.getBody());
                return "Error: Could not determine category. Status: " + response.getStatusCode();
            }
        } catch (Exception e) {
            System.err.println("Exception during API call to Hugging Face (Categorization): " + e.getMessage());
            e.printStackTrace();
            return "Error: Exception occurred while suggesting category.";
        }
    }
}
