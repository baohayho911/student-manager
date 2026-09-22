package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.AdministrativeClassRequest;
import com.example.studentmanagement.entity.AdministrativeClass;
import com.example.studentmanagement.service.AdministrativeClassService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class AdministrativeClassController {

    private final AdministrativeClassService classService;

    public AdministrativeClassController(
            AdministrativeClassService classService
    ) {
        this.classService = classService;
    }

    @GetMapping
    public List<AdministrativeClass> getClasses(
            @RequestParam(name = "keyword", defaultValue = "") String keyword
    ) {
        return classService.getClasses(keyword);
    }

    @GetMapping("/{id}")
    public AdministrativeClass getClassById(
            @PathVariable("id") Long id
    ) {
        return classService.getClassById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdministrativeClass createClass(
            @Valid @RequestBody AdministrativeClassRequest request
    ) {
        return classService.createClass(request);
    }

    @PutMapping("/{id}")
    public AdministrativeClass updateClass(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdministrativeClassRequest request
    ) {
        return classService.updateClass(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClass(@PathVariable("id") Long id) {
        classService.deleteClass(id);
    }
}
