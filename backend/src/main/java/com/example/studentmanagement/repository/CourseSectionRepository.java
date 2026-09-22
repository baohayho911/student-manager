package com.example.studentmanagement.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.studentmanagement.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;

public interface CourseSectionRepository
        extends JpaRepository<CourseSection, Long>,
        JpaSpecificationExecutor<CourseSection> {

    List<CourseSection> findBySectionCodeContainingIgnoreCaseOrderByIdAsc(
            String sectionCode
    );

    boolean existsBySectionCodeAndIdNot(String sectionCode, Long id);

    @Query(
            value = "SELECT COUNT(*) FROM enrollments WHERE course_section_id = ?1",
            nativeQuery = true
    )
    long countAllEnrollments(Long sectionId);

    @Query(
            value = """
                SELECT COUNT(*)
                FROM enrollments
                WHERE course_section_id = ?1
                  AND status <> 'CANCELLED'
                """,
            nativeQuery = true
    )
    long countNonCancelledEnrollments(Long sectionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CourseSection c WHERE c.id = :id")
    Optional<CourseSection> findByIdForUpdate(
            @Param("id") Long id
    );
}
