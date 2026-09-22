package com.example.studentmanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "administrative_classes")
public class AdministrativeClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_code", nullable = false, length = 30)
    private String classCode;

    @Column(name = "class_name", nullable = false, length = 150)
    private String className;

    @Column(name = "course_year", nullable = false)
    private Integer courseYear;

    @Column(name = "major_id", nullable = false)
    private Long majorId;

    public AdministrativeClass() {
    }

    public Long getId() {
        return id;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getCourseYear() {
        return courseYear;
    }

    public void setCourseYear(Integer courseYear) {
        this.courseYear = courseYear;
    }

    public Long getMajorId() {
        return majorId;
    }

    public void setMajorId(Long majorId) {
        this.majorId = majorId;
    }
}