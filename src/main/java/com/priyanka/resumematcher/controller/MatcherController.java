package com.priyanka.resumematcher.controller;

import com.priyanka.resumematcher.model.MatchResult;
import com.priyanka.resumematcher.service.MatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MatcherController {

    @Autowired
    private MatcherService matcherService;

    // Home page
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Handle form submission
    @PostMapping("/match")
    public String match(
            @RequestParam("resumeText") String resumeText,
            @RequestParam("jobDescription") String jobDescription,
            Model model) {

        MatchResult result = matcherService.analyzeMatch(resumeText, jobDescription);

        model.addAttribute("score", result.getMatchScore());
        model.addAttribute("missingSkills", result.getMissingSkills());
        model.addAttribute("resumeText", resumeText);
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
