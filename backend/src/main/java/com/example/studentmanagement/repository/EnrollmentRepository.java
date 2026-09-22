package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository
        extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudentIdAndCourseSectionId(
            Long studentId,
            Long courseSectionId
    );

    @Query("""
            SELECT e FROM Enrollment e
            WHERE (:studentId IS NULL OR e.studentId = :studentId)
              AND (:sectionId IS NULL OR e.courseSectionId = :sectionId)
            ORDER BY e.id ASC
            """)
    List<Enrollment> search(
            @Param("studentId") Long studentId,
            @Param("sectionId") Long sectionId
    );

    @Query(value = """
            SELECT course_section_id
            FROM enrollments
            WHERE id = :id
            """, nativeQuery = true)
    Optional<Long> findSectionIdByEnrollmentId(
            @Param("id") Long id
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM scores
            WHERE enrollment_id = :id
            """, nativeQuery = true)
    long countScores(
            @Param("id") Long enrollmentId
    );
}