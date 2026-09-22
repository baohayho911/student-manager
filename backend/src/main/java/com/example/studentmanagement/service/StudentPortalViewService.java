package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.PageResponse;
import com.example.studentmanagement.entity.CourseSection;
import com.example.studentmanagement.entity.Enrollment;
import com.example.studentmanagement.repository.AdministrativeClassRepository;
import com.example.studentmanagement.repository.CourseSectionRepository;
import com.example.studentmanagement.repository.EnrollmentRepository;
import com.example.studentmanagement.repository.SemesterRepository;
import com.example.studentmanagement.repository.SubjectRepository;
import com.example.studentmanagement.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentPortalViewService {

    private final PortalService portalService;
    private final CourseSectionService sectionService;
    private final CourseSectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;
    private final AdministrativeClassRepository classRepository;

    public StudentPortalViewService(
            PortalService portalService,
            CourseSectionService sectionService,
            CourseSectionRepository sectionRepository,
            EnrollmentRepository enrollmentRepository,
            SubjectRepository subjectRepository,
            SemesterRepository semesterRepository,
            TeacherRepository teacherRepository,
            AdministrativeClassRepository classRepository
    ) {
        this.portalService = portalService;
        this.sectionService = sectionService;
        this.sectionRepository = sectionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.subjectRepository = subjectRepository;
        this.semesterRepository = semesterRepository;
        this.teacherRepository = teacherRepository;
        this.classRepository = classRepository;
    }

    public ProfileView getProfile(String username) {
        var student = portalService.student(username);

        var administrativeClass = classRepository
                .findById(student.getClassId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lớp hành chính"
                ));

        return new ProfileView(
                student.getStudentCode(),
                student.getFullName(),
                student.getDateOfBirth(),
                student.getGender() == null
                        ? null
                        : student.getGender().name(),
                student.getEmail(),
                student.getPhone(),
                student.getAddress(),
                administrativeClass.getClassCode(),
                administrativeClass.getClassName()
        );
    }

    public PageResponse<SectionView> getOpenSections(
            String username,
            String keyword,
            int page,
            int size
    ) {
        // Kiểm tra tài khoản có hồ sơ sinh viên hợp lệ.
        portalService.student(username);

        var result = sectionService.getSections(
                keyword,
                null,
                null,
                null,
                CourseSection.Status.OPEN,
                page,
                size
        );

        var content = result.content()
                .stream()
                .map(this::toSectionView)
                .toList();

        return new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last()
        );
    }

    public List<EnrollmentView> getEnrollments(String username) {
        var student = portalService.student(username);

        // Chỉ lấy đăng ký của sinh viên đang đăng nhập.
        return enrollmentRepository
                .search(student.getId(), null)
                .stream()
                .map(this::toEnrollmentView)
                .toList();
    }

    private EnrollmentView toEnrollmentView(Enrollment enrollment) {
        var section = sectionRepository
                .findById(enrollment.getCourseSectionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lớp học phần"
                ));

        return new EnrollmentView(
                enrollment.getId(),
                enrollment.getRegisteredAt(),
                enrollment.getStatus().name(),
                toSectionView(section)
        );
    }

    private SectionView toSectionView(CourseSection section) {
        var subject = subjectRepository
                .findById(section.getSubjectId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy môn học"
                ));

        var semester = semesterRepository
                .findById(section.getSemesterId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy học kỳ"
                ));

        String teacherName = "Chưa phân công";

        if (section.getTeacherId() != null) {
            teacherName = teacherRepository
                    .findById(section.getTeacherId())
                    .map(teacher -> teacher.getFullName())
                    .orElse("Chưa xác định");
        }

        long registeredCount = sectionRepository
                .countNonCancelledEnrollments(section.getId());

        long availableSeats = Math.max(
                0L,
                (long) section.getMaximumStudents() - registeredCount
        );

        return new SectionView(
                section.getId(),
                section.getSectionCode(),
                subject.getSubjectCode(),
                subject.getSubjectName(),
                subject.getCredits(),
                semester.getSemesterName(),
                semester.getAcademicYear(),
                teacherName,
                section.getRoom(),
                section.getSchedule(),
                section.getMaximumStudents(),
                registeredCount,
                availableSeats,
                section.getStatus().name()
        );
    }

    public record ProfileView(
            String studentCode,
            String fullName,
            LocalDate dateOfBirth,
            String gender,
            String email,
            String phone,
            String address,
            String classCode,
            String className
    ) {
    }

    public record SectionView(
            Long id,
            String sectionCode,
            String subjectCode,
            String subjectName,
            Integer credits,
            String semesterName,
            String academicYear,
            String teacherName,
            String room,
            String schedule,
            Integer maximumStudents,
            long registeredCount,
            long availableSeats,
            String status
    ) {
    }

    public record EnrollmentView(
            Long id,
            LocalDateTime registeredAt,
            String status,
            SectionView section
    ) {
    }
}