package com.priyanka.resumematcher.controller;

import com.priyanka.resumematcher.model.MatchResult;
import com.priyanka.resumematcher.service.MatcherService;
import com.priyanka.resumematcher.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class MatcherController {

    @Autowired
    private MatcherService matcherService;

    @Autowired
    private PdfService pdfService;

    // Home page
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Handle form submission with PDF upload
    @PostMapping("/match")
    public String match(
            @RequestParam("resumeFile") MultipartFile resumeFile,
            @RequestParam("resumeText") String resumeText,
            @RequestParam("jobDescription") String jobDescription,
            Model model) {

        // If PDF uploaded, extract text from it
        String finalResumeText = resumeText;
        if (resumeFile != null && !resumeFile.isEmpty()) {
            String extractedText = pdfService.extractText(resumeFile);
            if (!extractedText.isEmpty()) {
                finalResumeText = extractedText;
            }
        }

        MatchResult result = matcherService.analyzeMatch(finalResumeText, jobDescription);

        model.addAttribute("score", result.getMatchScore());
        model.addAttribute("missingSkills", result.getMissingSkills());
        model.addAttribute("strengths", result.getStrengths());
        model.addAttribute("suggestion", result.getSuggestion());
        model.addAttribute("resumeText", finalResumeText);
        model.addAttribute("jobDescription", jobDescription);

        return "result";
    }

    // History page
    @GetMapping("/history")
    public String history(Model model) {
        List<MatchResult> results = matcherService.getAllResults();
        model.addAttribute("results", results);
        return "history";
    }
}