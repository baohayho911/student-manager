package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.SubjectRequest;
import com.example.studentmanagement.entity.Subject;
import com.example.studentmanagement.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<Subject> getSubjects(
            @RequestParam(name = "keyword", defaultValue = "") String keyword
    ) {
        return subjectService.getSubjects(keyword);
    }

    @GetMapping("/{id}")
    public Subject getSubject(@PathVariable("id") Long id) {
        return subjectService.getSubject(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Subject createSubject(
            @Valid @RequestBody SubjectRequest request
    ) {
        return subjectService.createSubject(request);
    }

    @PutMapping("/{id}")
    public Subject updateSubject(
            @PathVariable("id") Long id,
            @Valid @RequestBody SubjectRequest request
    ) {
        return subjectService.updateSubject(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubject(@PathVariable("id") Long id) {
        subjectService.deleteSubject(id);
    }
}
