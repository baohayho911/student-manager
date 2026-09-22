package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.ScoreRequest;
import com.example.studentmanagement.entity.*;
import com.example.studentmanagement.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PortalService {

    private final UserAccountRepository accountRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseSectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ScoreService scoreService;

    public PortalService(
            UserAccountRepository accountRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            CourseSectionRepository sectionRepository,
            EnrollmentRepository enrollmentRepository,
            ScoreService scoreService
    ) {
        this.accountRepository = accountRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.sectionRepository = sectionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.scoreService = scoreService;
    }

    public Student student(String username) {
        return studentRepository.findByUserId(accountId(username))
                .orElseThrow(() -> forbidden(
                        "Tài khoản chưa liên kết hồ sơ sinh viên"
                ));
    }

    public Teacher teacher(String username) {
        return teacherRepository.findByUserId(accountId(username))
                .orElseThrow(() -> forbidden(
                        "Tài khoản chưa liên kết hồ sơ giảng viên"
                ));
    }

    public Enrollment requireStudentEnrollment(
            String username,
            Long enrollmentId
    ) {
        Student student = student(username);
        Enrollment enrollment = findEnrollment(enrollmentId);

        if (!student.getId().equals(enrollment.getStudentId())) {
            throw forbidden("Đăng ký không thuộc sinh viên hiện tại");
        }

        return enrollment;
    }

    public CourseSection requireTeacherSection(
            String username,
            Long sectionId
    ) {
        Teacher teacher = teacher(username);

        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> notFound("Không tìm thấy lớp học phần"));

        if (!teacher.getId().equals(section.getTeacherId())) {
            throw forbidden("Bạn không được phân công lớp học phần này");
        }

        return section;
    }

    public void requireTeacherEnrollment(
            String username,
            Long enrollmentId
    ) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        requireTeacherSection(username, enrollment.getCourseSectionId());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Score saveTeacherScore(
            String username,
            Long enrollmentId,
            ScoreRequest request
    ) {
        Long sectionId = enrollmentRepository
                .findSectionIdByEnrollmentId(enrollmentId)
                .orElseThrow(() -> notFound("Không tìm thấy đăng ký"));

        // Khóa lớp trước khi kiểm tra phân công và ghi điểm.
        CourseSection section = sectionRepository
                .findByIdForUpdate(sectionId)
                .orElseThrow(() -> notFound("Không tìm thấy lớp học phần"));

        Teacher teacher = teacher(username);

        if (!teacher.getId().equals(section.getTeacherId())) {
            throw forbidden("Bạn không được nhập điểm lớp học phần này");
        }

        return scoreService.saveScore(enrollmentId, request);
    }

    private Long accountId(String username) {
        return accountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> forbidden("Tài khoản không tồn tại"))
                .getId();
    }

    private Enrollment findEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> notFound("Không tìm thấy đăng ký"));
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}