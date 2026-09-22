package com.example.studentmanagement.service;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final NamedParameterJdbcTemplate jdbc;
    private final PortalService portalService;

    public DashboardService(
            DataSource dataSource,
            PortalService portalService
    ) {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
        this.portalService = portalService;
    }

    public DashboardResponse getAdminDashboard() {
        return build("ADMIN", null, null);
    }

    public DashboardResponse getTeacherDashboard(String username) {
        var teacher = portalService.teacher(username);
        return build("TEACHER", teacher.getId(), null);
    }

    public DashboardResponse getStudentDashboard(String username) {
        var student = portalService.student(username);
        return build("STUDENT", null, student.getId());
    }

    private DashboardResponse build(
            String role,
            Long teacherId,
            Long studentId
    ) {
        Map<String, Object> params = new HashMap<>();

        String enrollmentScope = "";
        String sectionScope = "";

        if (teacherId != null) {
            params.put("teacherId", teacherId);
            enrollmentScope = " AND c.teacher_id = :teacherId ";
            sectionScope = " AND c.teacher_id = :teacherId ";
        }

        if (studentId != null) {
            params.put("studentId", studentId);
            enrollmentScope = " AND e.student_id = :studentId ";
        }

        String enrollmentFrom = """
                FROM enrollments e
                JOIN course_sections c ON c.id = e.course_section_id
                LEFT JOIN scores s ON s.enrollment_id = e.id
                WHERE e.status <> 'CANCELLED'
                """ + enrollmentScope;

        String summarySql = """
                SELECT
                    COUNT(e.id) AS enrollment_count,
                    COUNT(DISTINCT e.student_id) AS student_count,
                    COALESCE(SUM(
                        CASE WHEN s.total_score IS NOT NULL
                             THEN 1 ELSE 0 END
                    ), 0) AS graded_count,
                    COALESCE(SUM(
                        CASE WHEN s.total_score >= 4
                             THEN 1 ELSE 0 END
                    ), 0) AS at_least_four
                """ + enrollmentFrom;

        RawStats raw = jdbc.queryForObject(
                summarySql,
                params,
                (rs, rowNum) -> new RawStats(
                        rs.getLong("enrollment_count"),
                        rs.getLong("student_count"),
                        rs.getLong("graded_count"),
                        rs.getLong("at_least_four")
                )
        );

        if (raw == null) {
            throw new IllegalStateException("Không đọc được thống kê");
        }

        long ungraded = raw.enrollments() - raw.graded();

        BigDecimal completionRate = raw.enrollments() == 0
                ? null
                : BigDecimal.valueOf(raw.graded())
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(raw.enrollments()),
                        1,
                        RoundingMode.HALF_UP
                );

        Stats stats = new Stats(
                raw.enrollments(),
                raw.graded(),
                ungraded,
                raw.atLeastFour(),
                completionRate
        );

        List<Metric> metrics;

        if ("ADMIN".equals(role)) {
            metrics = List.of(
                    new Metric(
                            "students",
                            "Sinh viên",
                            count("SELECT COUNT(*) FROM students", Map.of())
                    ),
                    new Metric(
                            "teachers",
                            "Giảng viên",
                            count("SELECT COUNT(*) FROM teachers", Map.of())
                    ),
                    new Metric(
                            "subjects",
                            "Môn học",
                            count("SELECT COUNT(*) FROM subjects", Map.of())
                    ),
                    new Metric(
                            "sections",
                            "Lớp học phần",
                            count("SELECT COUNT(*) FROM course_sections", Map.of())
                    )
            );
        } else if ("TEACHER".equals(role)) {
            metrics = List.of(
                    new Metric(
                            "sections",
                            "Lớp được phân công",
                            count(
                                    """
                                    SELECT COUNT(*)
                                    FROM course_sections
                                    WHERE teacher_id = :teacherId
                                    """,
                                    params
                            )
                    ),
                    new Metric(
                            "students",
                            "Sinh viên khác nhau",
                            raw.students()
                    ),
                    new Metric(
                            "enrollments",
                            "Lượt đăng ký còn hiệu lực",
                            raw.enrollments()
                    ),
                    new Metric(
                            "ungraded",
                            "Lượt chưa có điểm",
                            ungraded
                    )
            );
        } else {
            metrics = List.of(
                    new Metric(
                            "enrollments",
                            "Học phần đăng ký còn hiệu lực",
                            raw.enrollments()
                    ),
                    new Metric(
                            "graded",
                            "Học phần đã có điểm",
                            raw.graded()
                    ),
                    new Metric(
                            "ungraded",
                            "Học phần chưa có điểm",
                            ungraded
                    ),
                    new Metric(
                            "atLeastFour",
                            "Học phần có tổng kết ≥ 4",
                            raw.atLeastFour()
                    )
            );
        }

        List<GradeBucket> distribution = readDistribution(
                enrollmentFrom,
                params
        );

        List<MissingClass> missingClasses =
                "STUDENT".equals(role)
                        ? List.of()
                        : readMissingClasses(sectionScope, params);

        String scope = switch (role) {
            case "ADMIN" -> "Toàn hệ thống, tất cả học kỳ";
            case "TEACHER" -> "Các lớp được phân công, tất cả học kỳ";
            default -> "Các đăng ký của bạn, tất cả học kỳ";
        };

        return new DashboardResponse(
                role,
                scope,
                Instant.now(),
                metrics,
                stats,
                distribution,
                missingClasses
        );
    }

    private List<GradeBucket> readDistribution(
            String enrollmentFrom,
            Map<String, Object> params
    ) {
        String sql = """
                SELECT
                    COALESCE(s.grade_letter, 'Chưa xác định') AS letter,
                    COUNT(*) AS amount
                """
                + enrollmentFrom
                + """
                 AND s.total_score IS NOT NULL
                 GROUP BY s.grade_letter
                """;

        List<GradeBucket> rows = jdbc.query(
                sql,
                params,
                (rs, rowNum) -> new GradeBucket(
                        rs.getString("letter"),
                        rs.getLong("amount")
                )
        );

        Map<String, Long> counts = new LinkedHashMap<>();

        for (String letter : List.of(
                "A", "B+", "B", "C+", "C", "D+", "D", "F"
        )) {
            counts.put(letter, 0L);
        }

        for (GradeBucket row : rows) {
            counts.merge(row.letter(), row.count(), Long::sum);
        }

        List<GradeBucket> result = new ArrayList<>();

        counts.forEach((letter, amount) ->
                result.add(new GradeBucket(letter, amount))
        );

        return result;
    }

    private List<MissingClass> readMissingClasses(
            String sectionScope,
            Map<String, Object> params
    ) {
        String sql = """
                SELECT
                    c.section_code,
                    sub.subject_name,
                    sem.semester_name,
                    sem.academic_year,
                    COUNT(e.id) AS enrollment_count,
                    COALESCE(SUM(
                        CASE WHEN s.total_score IS NOT NULL
                             THEN 1 ELSE 0 END
                    ), 0) AS graded_count,
                    COUNT(e.id) - COALESCE(SUM(
                        CASE WHEN s.total_score IS NOT NULL
                             THEN 1 ELSE 0 END
                    ), 0) AS missing_count
                FROM course_sections c
                JOIN subjects sub ON sub.id = c.subject_id
                JOIN semesters sem ON sem.id = c.semester_id
                LEFT JOIN enrollments e
                    ON e.course_section_id = c.id
                    AND e.status <> 'CANCELLED'
                LEFT JOIN scores s ON s.enrollment_id = e.id
                WHERE 1 = 1
                """
                + sectionScope
                + """
                 GROUP BY
                    c.id,
                    c.section_code,
                    sub.subject_name,
                    sem.semester_name,
                    sem.academic_year
                 HAVING missing_count > 0
                 ORDER BY missing_count DESC, c.id DESC
                 LIMIT 10
                """;

        return jdbc.query(
                sql,
                params,
                (rs, rowNum) -> new MissingClass(
                        rs.getString("section_code"),
                        rs.getString("subject_name"),
                        rs.getString("semester_name")
                                + " - "
                                + rs.getString("academic_year"),
                        rs.getLong("enrollment_count"),
                        rs.getLong("graded_count"),
                        rs.getLong("missing_count")
                )
        );
    }

    private long count(String sql, Map<String, ?> params) {
        Long value = jdbc.queryForObject(sql, params, Long.class);
        return value == null ? 0L : value;
    }

    private record RawStats(
            long enrollments,
            long students,
            long graded,
            long atLeastFour
    ) {
    }

    public record Metric(
            String key,
            String label,
            long value
    ) {
    }

    public record Stats(
            long enrollments,
            long graded,
            long ungraded,
            long atLeastFour,
            BigDecimal completionRate
    ) {
    }

    public record GradeBucket(
            String letter,
            long count
    ) {
    }

    public record MissingClass(
            String sectionCode,
            String subjectName,
            String semester,
            long enrollments,
            long graded,
            long missing
    ) {
    }

    public record DashboardResponse(
            String role,
            String scope,
            Instant generatedAt,
            List<Metric> metrics,
            Stats stats,
            List<GradeBucket> distribution,
            List<MissingClass> missingClasses
    ) {
    }
}
