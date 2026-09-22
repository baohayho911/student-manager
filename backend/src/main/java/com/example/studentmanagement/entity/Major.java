package com.example.studentmanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "majors")
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "major_code",
            nullable = false, unique = true, length = 20)
    private String majorCode;

    @Column(name = "major_name",
            nullable = false, length = 150)
    private String majorName;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    public Major() {
    }

    public Long getId() {
        return id;
    }

    public String getMajorCode() {
        return majorCode;
    }

    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
