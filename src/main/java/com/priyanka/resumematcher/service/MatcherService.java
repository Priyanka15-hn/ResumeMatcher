package com.priyanka.resumematcher.service;

import com.priyanka.resumematcher.model.MatchResult;
import com.priyanka.resumematcher.repository.MatchResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatcherService {

    @Autowired
    private MatchResultRepository repository;

    @Autowired
    private GeminiService geminiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MatchResult analyzeMatch(String resumeText, String jobDescription) {
        try {
            // Call Gemini AI
            String aiResponse = geminiService.analyzeWithAI(resumeText, jobDescription);

            // Parse JSON response
            JsonNode json = objectMapper.readTree(aiResponse);

            double score = json.path("score").asDouble();

            // Extract missing skills
            List<String> missingList = new ArrayList<>();
            json.path("missingSkills").forEach(s -> missingList.add(s.asText()));

            // Extract strengths
            List<String> strengthsList = new ArrayList<>();
            json.path("strengths").forEach(s -> strengthsList.add(s.asText()));

            String suggestion = json.path("suggestion").asText();

            // Save to database
            MatchResult result = new MatchResult();
            result.setResumeText(resumeText);
            result.setJobDescription(jobDescription);
            result.setMatchScore(score);
            result.setMissingSkills(String.join(", ", missingList));
            result.setStrengths(String.join(", ", strengthsList));
            result.setSuggestion(suggestion);

            return repository.save(result);

        } catch (Exception e) {
            // Fallback if AI fails
            MatchResult result = new MatchResult();
            result.setResumeText(resumeText);
            result.setJobDescription(jobDescription);
            result.setMatchScore(0.0);
            result.setMissingSkills("Error analyzing resume");
            result.setStrengths("Please try again");
            result.setSuggestion("Could not connect to AI service");
            return repository.save(result);
        }
    }

    public List<MatchResult> getAllResults() {
        return repository.findAll();
    }
}