package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubjectRepository
        extends JpaRepository<Subject, Long> {

    List<Subject>
    findBySubjectCodeContainingIgnoreCaseOrSubjectNameContainingIgnoreCaseOrderByIdAsc(
            String subjectCode,
            String subjectName
    );

    boolean existsBySubjectCodeAndIdNot(String subjectCode, Long id);

    @Query(
            value = "SELECT COUNT(*) FROM course_sections WHERE subject_id = ?1",
            nativeQuery = true
    )
    long countCourseSectionsBySubjectId(Long subjectId);
}