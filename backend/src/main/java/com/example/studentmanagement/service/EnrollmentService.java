package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.EnrollmentRequest;
import com.example.studentmanagement.entity.CourseSection;
import com.example.studentmanagement.entity.Enrollment;
import com.example.studentmanagement.repository.CourseSectionRepository;
import com.example.studentmanagement.repository.EnrollmentRepository;
import com.example.studentmanagement.repository.StudentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseSectionRepository sectionRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            CourseSectionRepository sectionRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollments(
            Long studentId,
            Long courseSectionId
    ) {
        if (studentId != null && studentId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID sinh viên phải lớn hơn 0"
            );
        }

        if (courseSectionId != null && courseSectionId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID lớp học phần phải lớn hơn 0"
            );
        }

        return enrollmentRepository.search(
                studentId,
                courseSectionId
        );
    }

    @Transactional(readOnly = true)
    public Enrollment getEnrollment(Long id) {
        return findEnrollment(id);
    }

    public Enrollment register(EnrollmentRequest request) {

        // Khóa lớp trước khi kiểm tra trạng thái và số chỗ.
        CourseSection section =
                lockSection(request.courseSectionId());

        if (!studentRepository.existsById(request.studentId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sinh viên không tồn tại"
            );
        }

        if (section.getStatus() != CourseSection.Status.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lớp học phần hiện không mở đăng ký"
            );
        }

        Enrollment existing = enrollmentRepository
                .findByStudentIdAndCourseSectionId(
                        request.studentId(),
                        request.courseSectionId()
                )
                .orElse(null);

        if (existing != null
                && existing.getStatus() != Enrollment.Status.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sinh viên đã đăng ký lớp học phần này"
            );
        }

        // Bảo vệ dữ liệu nếu bản ghi đã hủy vẫn có điểm.
        if (existing != null
                && enrollmentRepository.countScores(existing.getId()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Đăng ký đã có bản ghi điểm, không thể đăng ký lại"
            );
        }

        long currentStudents = sectionRepository
                .countNonCancelledEnrollments(section.getId());

        if (currentStudents >= section.getMaximumStudents()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lớp học phần đã đủ sĩ số"
            );
        }

        Enrollment enrollment;

        if (existing == null) {
            enrollment = new Enrollment();
            enrollment.setStudentId(request.studentId());
            enrollment.setCourseSectionId(request.courseSectionId());
        } else {
            enrollment = existing;
        }

        enrollment.setStatus(Enrollment.Status.STUDYING);
        enrollment.setRegisteredAt(LocalDateTime.now());

        return enrollmentRepository.saveAndFlush(enrollment);
    }

    public Enrollment cancel(Long id) {

        // Lấy ID lớp trước; chưa tải entity đăng ký vào bộ nhớ.
        Long sectionId = enrollmentRepository
                .findSectionIdByEnrollmentId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đăng ký có ID: " + id
                ));

        // Dùng cùng thứ tự khóa như khi đăng ký.
        CourseSection section = lockSection(sectionId);

        Enrollment enrollment = findEnrollment(id);

        // Hủy lại một bản ghi đã hủy vẫn trả về trạng thái hiện tại.
        if (enrollment.getStatus() == Enrollment.Status.CANCELLED) {
            return enrollment;
        }

        if (enrollment.getStatus() == Enrollment.Status.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể hủy đăng ký đã hoàn thành"
            );
        }

        if (section.getStatus() != CourseSection.Status.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Chỉ được hủy khi lớp đang mở đăng ký"
            );
        }

        if (enrollmentRepository.countScores(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Đăng ký đã có bản ghi điểm, không thể hủy"
            );
        }

        enrollment.setStatus(Enrollment.Status.CANCELLED);

        return enrollmentRepository.saveAndFlush(enrollment);
    }

    private Enrollment findEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đăng ký có ID: " + id
                ));
    }

    private CourseSection lockSection(Long id) {
        return sectionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lớp học phần có ID: " + id
                ));
    }
}
