package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.PageResponse;
import com.example.studentmanagement.util.PageSupport;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.repository.StudentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<Student> getStudents(
            String keyword,
            Long classId,
            int page,
            int size
    ) {
        Pageable pageable = PageSupport.createPageable(page, size);

        PageSupport.checkOptionalId(classId, "ID lớp");

        String pattern = PageSupport.keywordPattern(keyword);

        Specification<Student> specification = (root, query, cb) -> {

            Predicate matchesKeyword = cb.or(
                    cb.like(
                            cb.lower(root.<String>get("studentCode")),
                            pattern,
                            '!'
                    ),
                    cb.like(
                            cb.lower(root.<String>get("fullName")),
                            pattern,
                            '!'
                    )
            );

            if (classId == null) {
                return matchesKeyword;
            }

            return cb.and(
                    matchesKeyword,
                    cb.equal(root.get("classId"), classId)
            );
        };

        return PageResponse.from(
                studentRepository.findAll(specification, pageable)
        );
    }

    @Transactional(readOnly = true)
    public Student getStudent(Long id) {
        return findStudent(id);
    }

    public Student createStudent(StudentRequest request) {

        validateRequest(request, 0L);

        Student student = new Student();
        copyData(student, request);

        return studentRepository.saveAndFlush(student);
    }

    public Student updateStudent(Long id, StudentRequest request) {

        Student student = findStudent(id);

        validateRequest(request, id);
        copyData(student, request);

        return studentRepository.saveAndFlush(student);
    }

    public void deleteStudent(Long id) {

        Student student = findStudent(id);

        if (studentRepository.countEnrollmentsByStudentId(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa sinh viên đã có đăng ký học phần"
            );
        }

        studentRepository.delete(student);
        studentRepository.flush();
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy sinh viên có ID: " + id
                ));
    }

    private void validateRequest(
            StudentRequest request,
            Long currentId
    ) {
        if (studentRepository.existsByStudentCodeIgnoreCaseAndIdNot(
                request.studentCode().trim(),
                currentId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã sinh viên đã tồn tại"
            );
        }

        String email = normalizeEmail(request.email());

        if (email != null
                && studentRepository.existsByEmailIgnoreCaseAndIdNot(
                email,
                currentId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email đã được sinh viên khác sử dụng"
            );
        }

        if (studentRepository.countClassById(request.classId()) == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lớp hành chính không tồn tại"
            );
        }
    }

    private void copyData(
            Student student,
            StudentRequest request
    ) {
        student.setStudentCode(request.studentCode().trim());
        student.setFullName(request.fullName().trim());
        student.setDateOfBirth(request.dateOfBirth());
        student.setGender(request.gender());
        student.setEmail(normalizeEmail(request.email()));
        student.setPhone(trimToNull(request.phone()));
        student.setAddress(trimToNull(request.address()));
        student.setClassId(request.classId());
    }

    private String normalizeEmail(String value) {
        String email = trimToNull(value);

        return email == null
                ? null
                : email.toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}