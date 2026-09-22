package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SemesterRepository
        extends JpaRepository<Semester, Long> {

    List<Semester> findAllByOrderByIdAsc();

    boolean existsBySemesterNameAndAcademicYearAndIdNot(
            String semesterName,
            String academicYear,
            Long id
    );

    @Query(
            value = "SELECT COUNT(*) FROM course_sections WHERE semester_id = ?1",
            nativeQuery = true
    )
    long countSectionsBySemesterId(Long semesterId);
}
