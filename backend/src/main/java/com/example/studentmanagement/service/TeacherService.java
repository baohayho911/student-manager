package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.PageResponse;
import com.example.studentmanagement.util.PageSupport;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.studentmanagement.dto.TeacherRequest;
import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.repository.TeacherRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<Teacher> getTeachers(
            String keyword,
            Long departmentId,
            int page,
            int size
    ) {
        Pageable pageable = PageSupport.createPageable(page, size);

        PageSupport.checkOptionalId(departmentId, "ID khoa");

        String pattern = PageSupport.keywordPattern(keyword);

        Specification<Teacher> specification = (root, query, cb) -> {

            Predicate matchesKeyword = cb.or(
                    cb.like(
                            cb.lower(root.<String>get("teacherCode")),
                            pattern,
                            '!'
                    ),
                    cb.like(
                            cb.lower(root.<String>get("fullName")),
                            pattern,
                            '!'
                    )
            );

            if (departmentId == null) {
                return matchesKeyword;
            }

            return cb.and(
                    matchesKeyword,
                    cb.equal(root.get("departmentId"), departmentId)
            );
        };

        return PageResponse.from(
                teacherRepository.findAll(specification, pageable)
        );
    }

    @Transactional(readOnly = true)
    public Teacher getTeacher(Long id) {
        return findTeacher(id);
    }

    public Teacher createTeacher(TeacherRequest request) {

        validateRequest(request, 0L);

        Teacher teacher = new Teacher();
        copyData(teacher, request);

        return teacherRepository.saveAndFlush(teacher);
    }

    public Teacher updateTeacher(Long id, TeacherRequest request) {

        Teacher teacher = findTeacher(id);

        validateRequest(request, id);
        copyData(teacher, request);

        return teacherRepository.saveAndFlush(teacher);
    }

    public void deleteTeacher(Long id) {

        Teacher teacher = findTeacher(id);

        if (teacherRepository.countSectionsByTeacherId(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa giảng viên đang được phân công lớp học phần"
            );
        }

        teacherRepository.delete(teacher);
        teacherRepository.flush();
    }

    private Teacher findTeacher(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy giảng viên có ID: " + id
                ));
    }

    private void validateRequest(
            TeacherRequest request,
            Long currentId
    ) {
        if (teacherRepository.existsByTeacherCodeIgnoreCaseAndIdNot(
                request.teacherCode().trim(),
                currentId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã giảng viên đã tồn tại"
            );
        }

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (teacherRepository.existsByEmailIgnoreCaseAndIdNot(
                email,
                currentId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email đã được giảng viên khác sử dụng"
            );
        }

        if (teacherRepository.countDepartmentById(
                request.departmentId()
        ) == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Khoa không tồn tại"
            );
        }
    }

    private void copyData(
            Teacher teacher,
            TeacherRequest request
    ) {
        teacher.setTeacherCode(request.teacherCode().trim());
        teacher.setFullName(request.fullName().trim());
        teacher.setDateOfBirth(request.dateOfBirth());
        teacher.setGender(request.gender());

        teacher.setEmail(
                request.email().trim().toLowerCase(Locale.ROOT)
        );

        teacher.setPhone(trimToNull(request.phone()));
        teacher.setAcademicDegree(trimToNull(request.academicDegree()));
        teacher.setDepartmentId(request.departmentId());
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}