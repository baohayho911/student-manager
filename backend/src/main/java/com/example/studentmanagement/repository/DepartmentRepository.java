package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.Department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    List<Department>
    findByDepartmentCodeContainingIgnoreCaseOrDepartmentNameContainingIgnoreCaseOrderByIdAsc(
            String code,
            String name
    );

    boolean existsByDepartmentCodeIgnoreCaseAndIdNot(
            String departmentCode,
            Long id
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM majors
            WHERE department_id = ?1
            """, nativeQuery = true)
    long countMajors(Long departmentId);

    @Query(value = """
            SELECT COUNT(*)
            FROM teachers
            WHERE department_id = ?1
            """, nativeQuery = true)
    long countTeachers(Long departmentId);
}