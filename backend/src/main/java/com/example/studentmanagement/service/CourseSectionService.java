package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.PageResponse;
import com.example.studentmanagement.util.PageSupport;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;


import com.example.studentmanagement.dto.CourseSectionRequest;
import com.example.studentmanagement.entity.CourseSection;
import com.example.studentmanagement.repository.CourseSectionRepository;
import com.example.studentmanagement.repository.SemesterRepository;
import com.example.studentmanagement.repository.SubjectRepository;
import com.example.studentmanagement.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class CourseSectionService {

    private final CourseSectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SemesterRepository semesterRepository;

    public CourseSectionService(
            CourseSectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            TeacherRepository teacherRepository,
            SemesterRepository semesterRepository
    ) {
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.semesterRepository = semesterRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseSection> getSections(
            String keyword,
            Long semesterId,
            Long subjectId,
            Long teacherId,
            CourseSection.Status status,
            int page,
            int size
    ) {
        Pageable pageable = PageSupport.createPageable(page, size);

        PageSupport.checkOptionalId(semesterId, "ID học kỳ");
        PageSupport.checkOptionalId(subjectId, "ID môn học");
        PageSupport.checkOptionalId(teacherId, "ID giảng viên");

        String pattern = PageSupport.keywordPattern(keyword);

        Specification<CourseSection> specification = (root, query, cb) -> {

            List<Predicate> conditions = new ArrayList<>();

            conditions.add(
                    cb.like(
                            cb.lower(root.<String>get("sectionCode")),
                            pattern,
                            '!'
                    )
            );

            if (semesterId != null) {
                conditions.add(
                        cb.equal(root.get("semesterId"), semesterId)
                );
            }

            if (subjectId != null) {
                conditions.add(
                        cb.equal(root.get("subjectId"), subjectId)
                );
            }

            if (teacherId != null) {
                conditions.add(
                        cb.equal(root.get("teacherId"), teacherId)
                );
            }

            if (status != null) {
                conditions.add(
                        cb.equal(root.get("status"), status)
                );
            }

            return cb.and(conditions.toArray(new Predicate[0]));
        };

        return PageResponse.from(
                sectionRepository.findAll(specification, pageable)
        );
    }

    @Transactional(readOnly = true)
    public CourseSection getSection(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lớp học phần có ID: " + id
                ));
    }

    public CourseSection createSection(CourseSectionRequest request) {
        validateRequest(request, 0L);

        CourseSection section = new CourseSection();
        copyData(request, section);

        return sectionRepository.saveAndFlush(section);
    }

    public CourseSection updateSection(
            Long id,
            CourseSectionRequest request
    ) {
        CourseSection section = getSectionForUpdate(id);
        validateRequest(request, id);

        long totalEnrollments = sectionRepository.countAllEnrollments(id);

        boolean changedSubject =
                !Objects.equals(section.getSubjectId(), request.subjectId());



        boolean changedSemester =
                !Objects.equals(section.getSemesterId(), request.semesterId());

        if (totalEnrollments > 0 && (changedSubject || changedSemester)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không được đổi môn hoặc học kỳ khi lớp đã có đăng ký"
            );
        }

        long currentStudents =
                sectionRepository.countNonCancelledEnrollments(id);

        if (request.maximumStudents() < currentStudents) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sĩ số tối đa không được nhỏ hơn số đăng ký chưa hủy"
            );
        }

        copyData(request, section);

        return sectionRepository.saveAndFlush(section);
    }

    public void deleteSection(Long id) {
        CourseSection section = getSectionForUpdate(id);

        if (sectionRepository.countAllEnrollments(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa lớp học phần đã có đăng ký"
            );
        }

        sectionRepository.delete(section);
        sectionRepository.flush();
    }

    private void validateRequest(
            CourseSectionRequest request,
            Long currentId
    ) {
        if (sectionRepository.existsBySectionCodeAndIdNot(
                request.sectionCode().trim(), currentId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã lớp học phần đã tồn tại"
            );
        }

        if (!subjectRepository.existsById(request.subjectId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Môn học không tồn tại"
            );
        }

        if (!semesterRepository.existsById(request.semesterId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Học kỳ không tồn tại"
            );
        }

        if (request.teacherId() != null
                && !teacherRepository.existsById(request.teacherId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Giảng viên không tồn tại"
            );
        }
    }

    private void copyData(
            CourseSectionRequest request,
            CourseSection section
    ) {
        section.setSectionCode(request.sectionCode().trim());
        section.setSubjectId(request.subjectId());
        section.setTeacherId(request.teacherId());
        section.setSemesterId(request.semesterId());
        section.setRoom(trimToNull(request.room()));
        section.setSchedule(trimToNull(request.schedule()));
        section.setMaximumStudents(request.maximumStudents());
        section.setStatus(request.status());
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
    private CourseSection getSectionForUpdate(Long id) {
        return sectionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lớp học phần có ID: " + id
                ));
    }
}
