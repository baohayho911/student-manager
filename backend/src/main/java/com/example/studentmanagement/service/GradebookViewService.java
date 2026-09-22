package com.example.studentmanagement.service;

import com.example.studentmanagement.entity.CourseSection;
import com.example.studentmanagement.entity.Enrollment;
import com.example.studentmanagement.entity.Score;
import com.example.studentmanagement.repository.CourseSectionRepository;
import com.example.studentmanagement.repository.EnrollmentRepository;
import com.example.studentmanagement.repository.ScoreRepository;
import com.example.studentmanagement.repository.SemesterRepository;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class GradebookViewService {

    private final PortalService portalService;
    private final StudentPortalViewService studentPortalViewService;
    private final CourseSectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ScoreRepository scoreRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;

    public GradebookViewService(
            PortalService portalService,
            StudentPortalViewService studentPortalViewService,
            CourseSectionRepository sectionRepository,
            EnrollmentRepository enrollmentRepository,
            ScoreRepository scoreRepository,
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            SemesterRepository semesterRepository
    ) {
        this.portalService = portalService;
        this.studentPortalViewService = studentPortalViewService;
        this.sectionRepository = sectionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.scoreRepository = scoreRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.semesterRepository = semesterRepository;
    }

    public Gradebook getAdminGradebook(Long sectionId) {
        return buildGradebook(findSection(sectionId));
    }

    public Gradebook getTeacherGradebook(
            String username,
            Long sectionId
    ) {
        var teacher = portalService.teacher(username);
        var section = findSection(sectionId);

        if (!Objects.equals(section.getTeacherId(), teacher.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không được phân công giảng dạy lớp này"
            );
        }

        return buildGradebook(section);
    }

    public List<StudentGrade> getStudentGrades(String username) {
        // Service bài 20 chỉ trả đăng ký của sinh viên đang đăng nhập.
        return studentPortalViewService
                .getEnrollments(username)
                .stream()
                .filter(item -> !"CANCELLED".equals(item.status()))
                .map(item -> {
                    var section = findSection(item.section().id());
                    var view = item.section();

                    return new StudentGrade(
                            view.sectionCode(),
                            view.subjectName(),
                            view.credits(),
                            view.semesterName() + " - " + view.academicYear(),
                            section.getTx1Weight(),
                            section.getTx2Weight(),
                            section.getKthpWeight(),
                            findScore(item.id())
                    );
                })
                .toList();
    }

    private Gradebook buildGradebook(CourseSection section) {
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

        var rows = enrollmentRepository
                .search(null, section.getId())
                .stream()
                .filter(item ->
                        item.getStatus() != Enrollment.Status.CANCELLED
                )
                .map(item -> {
                    var student = studentRepository
                            .findById(item.getStudentId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Không tìm thấy sinh viên"
                            ));

                    return new GradeRow(
                            item.getId(),
                            student.getStudentCode(),
                            student.getFullName(),
                            item.getStatus().name(),
                            findScore(item.getId())
                    );
                })
                .toList();

        return new Gradebook(
                section.getId(),
                section.getSectionCode(),
                subject.getSubjectName(),
                semester.getSemesterName() + " - " + semester.getAcademicYear(),
                section.getTx1Weight(),
                section.getTx2Weight(),
                section.getKthpWeight(),
                section.isGradingConfigured(),
                rows
        );
    }

    private CourseSection findSection(Long sectionId) {
        if (sectionId == null || sectionId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID lớp học phần không hợp lệ"
            );
        }

        return sectionRepository
                .findById(sectionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lớp học phần"
                ));
    }

    private ScoreView findScore(Long enrollmentId) {
        return scoreRepository
                .findByEnrollmentId(enrollmentId)
                .filter(score -> score.getAverageScore() != null)
                .map(this::toScoreView)
                .orElse(null);
    }

    private ScoreView toScoreView(Score score) {
        return new ScoreView(
                score.getTx1Score(),
                score.getTx2Score(),
                score.getKthpScore(),
                score.getAverageScore(),
                score.getLetterGrade(),
                score.getNote()
        );
    }

    public record ScoreView(
            BigDecimal tx1Score,
            BigDecimal tx2Score,
            BigDecimal kthpScore,
            BigDecimal averageScore,
            String letterGrade,
            String note
    ) {
    }

    public record GradeRow(
            Long enrollmentId,
            String studentCode,
            String fullName,
            String enrollmentStatus,
            ScoreView score
    ) {
    }

    public record Gradebook(
            Long sectionId,
            String sectionCode,
            String subjectName,
            String semester,
            Integer tx1Weight,
            Integer tx2Weight,
            Integer kthpWeight,
            boolean gradingConfigured,
            List<GradeRow> rows
    ) {
    }

    public record StudentGrade(
            String sectionCode,
            String subjectName,
            Integer credits,
            String semester,
            Integer tx1Weight,
            Integer tx2Weight,
            Integer kthpWeight,
            ScoreView score
    ) {
    }
}