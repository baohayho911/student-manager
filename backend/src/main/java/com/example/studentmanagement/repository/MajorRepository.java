package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.Major;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MajorRepository extends JpaRepository<Major, Long> {

    boolean existsByMajorCodeIgnoreCaseAndIdNot(
            String majorCode,
            Long id
    );

    @Query("""
            SELECT m
            FROM Major m
            WHERE (
                LOWER(m.majorCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.majorName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (
                :departmentId IS NULL
                OR m.departmentId = :departmentId
            )
            ORDER BY m.id ASC
            """)
    List<Major> search(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM administrative_classes
            WHERE major_id = ?1
            """, nativeQuery = true)
    long countClasses(Long majorId);
}