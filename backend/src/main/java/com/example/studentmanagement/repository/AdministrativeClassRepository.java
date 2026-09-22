package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.AdministrativeClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdministrativeClassRepository
        extends JpaRepository<AdministrativeClass, Long> {

    List<AdministrativeClass>
    findByClassCodeContainingIgnoreCaseOrClassNameContainingIgnoreCaseOrderByIdAsc(
            String classCode,
            String className
    );

    boolean existsByClassCodeAndIdNot(String classCode, Long id);

    @Query(
            value = "SELECT COUNT(*) FROM majors WHERE id = ?1",
            nativeQuery = true
    )
    long countMajorById(Long majorId);

    @Query(
            value = "SELECT COUNT(*) FROM students WHERE class_id = ?1",
            nativeQuery = true
    )
    long countStudentsByClassId(Long classId);
}
