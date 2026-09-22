package com.example.studentmanagement.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.studentmanagement.entity.Teacher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeacherRepository
        extends JpaRepository<Teacher, Long>,
        JpaSpecificationExecutor<Teacher> {

    Optional<Teacher> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Teacher t WHERE t.id = :id")
    Optional<Teacher> findByIdForAccount(@Param("id") Long id);

    List<Teacher>
    findByTeacherCodeContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrderByIdAsc(
            String code,
            String name
    );

    boolean existsByTeacherCodeIgnoreCaseAndIdNot(
            String teacherCode,
            Long id
    );

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM departments
            WHERE id = ?1
            """, nativeQuery = true)
    long countDepartmentById(Long departmentId);

    @Query(value = """
            SELECT COUNT(*)
            FROM course_sections
            WHERE teacher_id = ?1
            """, nativeQuery = true)
    long countSectionsByTeacherId(Long teacherId);
}