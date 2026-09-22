package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.MajorRequest;
import com.example.studentmanagement.entity.Major;
import com.example.studentmanagement.service.MajorService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/majors")
public class MajorController {

    private final MajorService majorService;

    public MajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping
    public List<Major> getMajors(
            @RequestParam(name = "keyword", defaultValue = "")
            String keyword,

            @RequestParam(name = "departmentId", required = false)
            Long departmentId
    ) {
        return majorService.getMajors(keyword, departmentId);
    }

    @GetMapping("/{id}")
    public Major getMajor(
            @PathVariable("id") Long id
    ) {
        return majorService.getMajor(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Major createMajor(
            @Valid @RequestBody MajorRequest request
    ) {
        return majorService.createMajor(request);
    }

    @PutMapping("/{id}")
    public Major updateMajor(
            @PathVariable("id") Long id,
            @Valid @RequestBody MajorRequest request
    ) {
        return majorService.updateMajor(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMajor(
            @PathVariable("id") Long id
    ) {
        majorService.deleteMajor(id);
    }
}