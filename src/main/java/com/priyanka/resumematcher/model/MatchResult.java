package com.priyanka.resumematcher.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "match_result")
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String resumeText;

    @Lob
    private String jobDescription;

    private Double matchScore;

    private String missingSkills;

    private String strengths;

    @Lob
    private String suggestion;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}