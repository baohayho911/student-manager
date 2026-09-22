package com.example.studentmanagement.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.studentmanagement.entity.Student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;



public interface StudentRepository
        extends JpaRepository<Student, Long>,
        JpaSpecificationExecutor<Student>{

    Optional<Student> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Student s WHERE s.id = :id")
    Optional<Student> findByIdForAccount(@Param("id") Long id);

    List<Student>
    findByStudentCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrderByIdAsc(
            String code,
            String name
    );

    boolean existsByStudentCodeIgnoreCaseAndIdNot(
            String studentCode,
            Long id
    );

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM administrative_classes
            WHERE id = ?1
            """, nativeQuery = true)
    long countClassById(Long classId);

    @Query(value = """
            SELECT COUNT(*)
            FROM enrollments
            WHERE student_id = ?1
            """, nativeQuery = true)
    long countEnrollmentsByStudentId(Long studentId);
}