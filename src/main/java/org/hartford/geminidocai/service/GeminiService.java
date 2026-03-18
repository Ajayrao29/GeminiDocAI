package org.hartford.GeminiDocAI.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.GeminiDocAI.model.DocumentExtraction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public DocumentExtraction processDocument(String base64Data, String mimeType) throws Exception {
        String prompt = """
            Analyze the following document and:
            1. Identify the document type (e.g. Invoice, Insurance Policy, Driving License).
            2. Extract the following fields in JSON format:
               - documentType
               - name
               - policyNumber
               - date (format: YYYY-MM-DD if possible)
               - confidence_score (between 0 and 1)
            
            Return ONLY a valid JSON object.
            """;

        Map<String, Object> request = Map.of(
            "contents", new Object[]{
                Map.of(
                    "parts", new Object[]{
                        Map.of("text", prompt),
                        Map.of("inlineData", Map.of(
                            "mimeType", mimeType,
                            "data", base64Data
                        ))
                    }
                )
            }
        );

        String response = webClient.post()
            .uri(apiUrl + "?key=" + apiKey)
            .bodyValue(request)
            .retrieve()
            .onStatus(status -> status.isError(), clientResponse -> 
                clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                    logger.error("Gemini API Error: {} - {}", clientResponse.statusCode(), errorBody);
                    return Mono.error(new Exception("Gemini API Error: " + clientResponse.statusCode() + " " + errorBody));
                })
            )
            .bodyToMono(String.class)
            .block();

        return parseGeminiResponse(response);
    }

    private DocumentExtraction parseGeminiResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        String text = root.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
        
        // Clean markdown backticks if present (e.g., ```json ... ```)
        text = text.replaceAll("```json\\s?", "").replaceAll("```", "").trim();

        JsonNode extractedData = objectMapper.readTree(text);
        
        return DocumentExtraction.builder()
                .type(extractedData.path("documentType").asText(null))
                .name(extractedData.path("name").asText(null))
                .policyNumber(extractedData.path("policyNumber").asText(null))
                .date(extractedData.path("date").asText(null))
                .confidenceScore(extractedData.path("confidence_score").asDouble(0.0))
                .allExtractedFields(objectMapper.convertValue(extractedData, Map.class))
                .build();
    }
}
