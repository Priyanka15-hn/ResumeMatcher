package com.priyanka.resumematcher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public String analyzeWithAI(String resumeText, String jobDescription) {
        try {
            String prompt = """
                You are an expert resume analyzer. Analyze the following resume against the job description and provide:
                1. A match score out of 100
                2. Top 3 missing skills
                3. Top 3 strengths found in resume
                4. One specific suggestion to improve the resume for this job
                
                Resume:
                %s
                
                Job Description:
                %s
                
                Respond in this exact JSON format:
                {
                    "score": 85,
                    "missingSkills": ["skill1", "skill2", "skill3"],
                    "strengths": ["strength1", "strength2", "strength3"],
                    "suggestion": "your suggestion here"
                }
                Return only the JSON, no extra text.
                """.formatted(resumeText, jobDescription);

            String requestBody = """
                {
                    "contents": [{
                        "parts": [{
                            "text": "%s"
                        }]
                    }]
                }
                """.formatted(prompt.replace("\"", "\\\"")
                               .replace("\n", "\\n"));

            String response = webClient.post()
                .uri("/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            // Extract text from response
            JsonNode root = objectMapper.readTree(response);
            String aiText = root.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

            // Clean up response
            aiText = aiText.replace("```json", "").replace("```", "").trim();
            return aiText;

        } catch (Exception e) {
            return "{\"score\": 0, \"missingSkills\": [\"Error analyzing\"], \"strengths\": [\"Please try again\"], \"suggestion\": \"Could not connect to AI service\"}";
        }
    }
}
