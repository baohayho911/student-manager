package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.GradingPolicyRequest;
import com.example.studentmanagement.dto.ScoreRequest;

import com.example.studentmanagement.entity.CourseSection;
import com.example.studentmanagement.entity.Score;
import com.example.studentmanagement.service.ScoreService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @PutMapping("/policy/{sectionId}")
    public CourseSection updatePolicy(
            @PathVariable("sectionId") Long sectionId,
            @Valid @RequestBody GradingPolicyRequest request
    ) {
        return scoreService.updatePolicy(sectionId, request);
    }

    @GetMapping
    public List<Score> getScores(
            @RequestParam(name = "studentId", required = false)
            Long studentId,

            @RequestParam(name = "courseSectionId", required = false)
            Long courseSectionId
    ) {
        return scoreService.getScores(studentId, courseSectionId);
    }

    @GetMapping("/enrollment/{enrollmentId}")
    public Score getScoreByEnrollment(
            @PathVariable("enrollmentId") Long enrollmentId
    ) {
        return scoreService.getScoreByEnrollment(enrollmentId);
    }

    @PutMapping("/enrollment/{enrollmentId}")
    public Score saveScore(
            @PathVariable("enrollmentId") Long enrollmentId,
            @Valid @RequestBody ScoreRequest request
    ) {
        return scoreService.saveScore(enrollmentId, request);
    }
}
