package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.PageResponse;

import com.example.studentmanagement.dto.CourseSectionRequest;
import com.example.studentmanagement.entity.CourseSection;
import com.example.studentmanagement.service.CourseSectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-sections")
public class CourseSectionController {

    private final CourseSectionService sectionService;

    public CourseSectionController(CourseSectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public PageResponse<CourseSection> getSections(
            @RequestParam(name = "keyword", defaultValue = "")
            String keyword,

            @RequestParam(name = "semesterId", required = false)
            Long semesterId,

            @RequestParam(name = "subjectId", required = false)
            Long subjectId,

            @RequestParam(name = "teacherId", required = false)
            Long teacherId,

            @RequestParam(name = "status", required = false)
            CourseSection.Status status,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size
    ) {
        return sectionService.getSections(
                keyword,
                semesterId,
                subjectId,
                teacherId,
                status,
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public CourseSection getSection(@PathVariable("id") Long id) {
        return sectionService.getSection(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseSection createSection(
            @Valid @RequestBody CourseSectionRequest request
    ) {
        return sectionService.createSection(request);
    }

    @PutMapping("/{id}")
    public CourseSection updateSection(
            @PathVariable("id") Long id,
            @Valid @RequestBody CourseSectionRequest request
    ) {
        return sectionService.updateSection(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSection(@PathVariable("id") Long id) {
        sectionService.deleteSection(id);
    }
}
