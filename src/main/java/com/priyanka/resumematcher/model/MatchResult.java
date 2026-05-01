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

    @Column(columnDefinition = "LONGTEXT")
    private String resumeText;

    @Column(columnDefinition = "LONGTEXT")
    private String jobDescription;

    private Double matchScore;

    @Column(length = 1000)
    private String missingSkills;

    @Column(length = 1000)
    private String strengths;

    @Column(columnDefinition = "LONGTEXT")
    private String suggestion;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}