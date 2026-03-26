package SISA.example.SISA.service;

import SISA.example.SISA.model.AnalysisResponse.Finding;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    public List<String> getInsights(List<Finding> findings) {
        if (findings.isEmpty()) return List.of("Security posture is clean.");

        try {
            // 1. Prepare Context
            String context = findings.stream()
                    .map(f -> f.getType() + " detected on line " + f.getLine())
                    .collect(Collectors.joining(", "));

            String prompt = "You are a Senior SOC Analyst. Analyze these log findings: [" + context +
                    "]. Provide 2 short, professional, actionable security mitigations.";

            // 2. Build JSON
            String requestBody = """
            {
              "contents": [{
                "parts": [{"text": "%s"}]
              }]
            }
            """.formatted(prompt.replace("\"", "\\\""));

            HttpClient client = HttpClient.newHttpClient();

            // 3. THE FIX: Using the currently active 'gemini-2.5-flash' model
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 4. Send Request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("GOOGLE API REJECTED REQUEST: " + response.body());
                return List.of("Fallback: Rotate exposed secrets and review line-specific leaks.");
            }

            // 5. Parse Response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body());

            String aiText = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return List.of(aiText.trim());

        } catch (Exception e) {
            System.err.println("INTERNAL ERROR: " + e.getMessage());
            return List.of("Manual Review Required: Multiple critical leaks detected.");
        }
    }
}