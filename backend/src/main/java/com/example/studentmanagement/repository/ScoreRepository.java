package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.Score;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    Optional<Score> findByEnrollmentId(Long enrollmentId);

    @Query("""
            SELECT s
            FROM Score s, Enrollment e
            WHERE s.enrollmentId = e.id
              AND s.averageScore IS NOT NULL
              AND (:studentId IS NULL OR e.studentId = :studentId)
              AND (:sectionId IS NULL OR e.courseSectionId = :sectionId)
            ORDER BY s.id ASC
            """)
    List<Score> search(
            @Param("studentId") Long studentId,
            @Param("sectionId") Long sectionId
    );

    @Query("""
            SELECT COUNT(s)
            FROM Score s, Enrollment e
            WHERE s.enrollmentId = e.id
              AND e.courseSectionId = :sectionId
              AND s.tx1Score IS NOT NULL
            """)
    long countNewScoresBySection(
            @Param("sectionId") Long sectionId
    );
}