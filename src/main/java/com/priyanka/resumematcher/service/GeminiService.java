package com.priyanka.resumematcher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public class GeminiService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.groq.com")
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public String analyzeWithAI(String resumeText, String jobDescription) {
        try {
            String prompt = "You are an expert resume analyzer. Analyze the resume against the job description.\n" +
                "Resume: " + resumeText + "\n" +
                "Job Description: " + jobDescription + "\n" +
                "Respond ONLY in this exact JSON format with no extra text:\n" +
                "{\n" +
                "  \"score\": 85,\n" +
                "  \"missingSkills\": [\"skill1\", \"skill2\", \"skill3\"],\n" +
                "  \"strengths\": [\"strength1\", \"strength2\", \"strength3\"],\n" +
                "  \"suggestion\": \"one specific suggestion here\"\n" +
                "}";

            // Build request using Jackson — no manual JSON string building
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "llama-3.3-70b-versatile");
            requestBody.put("temperature", 0.3);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.set("messages", messages);

            String response = webClient.post()
            	    .uri("/openai/v1/chat/completions")
            	    .header("Content-Type", "application/json")
            	    .header("Authorization", "Bearer " + apiKey)
            	    .bodyValue(requestBody.toString())
            	    .retrieve()
            	    .onStatus(status -> status.is4xxClientError(), clientResponse ->
            	        clientResponse.bodyToMono(String.class)
            	            .doOnNext(body -> System.out.println("GROQ ERROR BODY: " + body))
            	            .flatMap(body -> reactor.core.publisher.Mono.error(new RuntimeException(body)))
            	    )
            	    .bodyToMono(String.class)
            	    .block();

            JsonNode root = objectMapper.readTree(response);
            String aiText = root.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

            aiText = aiText.replace("```json", "").replace("```", "").trim();
            return aiText;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Groq Error: " + e.getMessage());
            return "{\"score\": 0, \"missingSkills\": [\"Error analyzing\"], \"strengths\": [\"Please try again\"], \"suggestion\": \"Could not connect to AI service\"}";
        }
    }
}