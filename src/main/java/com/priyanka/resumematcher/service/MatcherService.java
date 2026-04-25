package com.priyanka.resumematcher.service;

import com.priyanka.resumematcher.model.MatchResult;
import com.priyanka.resumematcher.repository.MatchResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatcherService {

    @Autowired
    private MatchResultRepository repository;

    // Common tech keywords to match against
    private static final List<String> TECH_KEYWORDS = Arrays.asList(
        "java", "python", "spring", "springboot", "hibernate", "mysql",
        "sql", "rest", "api", "html", "css", "javascript", "react",
        "git", "maven", "docker", "linux", "oops", "jdbc", "jpa",
        "microservices", "aws", "mongodb", "bootstrap", "thymeleaf",
        "c", "c++", "data structures", "algorithms", "multithreading"
    );

    public MatchResult analyzeMatch(String resumeText, String jobDescription) {

        String resumeLower = resumeText.toLowerCase();
        String jobLower = jobDescription.toLowerCase();

        // Extract keywords from job description
        List<String> jobKeywords = new ArrayList<>();
        for (String keyword : TECH_KEYWORDS) {
            if (jobLower.contains(keyword)) {
                jobKeywords.add(keyword);
            }
        }

        // Check which job keywords are in resume
        List<String> matchedKeywords = new ArrayList<>();
        List<String> missingKeywords = new ArrayList<>();

        for (String keyword : jobKeywords) {
            if (resumeLower.contains(keyword)) {
                matchedKeywords.add(keyword);
            } else {
                missingKeywords.add(keyword);
            }
        }

        // Calculate match score
        double score = 0.0;
        if (!jobKeywords.isEmpty()) {
            score = ((double) matchedKeywords.size() / jobKeywords.size()) * 100;
        }

        // Round to 2 decimal places
        score = Math.round(score * 100.0) / 100.0;

        // Save to database
        MatchResult result = new MatchResult();
        result.setResumeText(resumeText);
        result.setJobDescription(jobDescription);
        result.setMatchScore(score);
        result.setMissingSkills(String.join(", ", missingKeywords));

        return repository.save(result);
    }

    public List<MatchResult> getAllResults() {
        return repository.findAll();
    }
}