package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.GradingPolicyRequest;
import com.example.studentmanagement.dto.ScoreRequest;

import com.example.studentmanagement.entity.CourseSection;
import com.example.studentmanagement.entity.Enrollment;
import com.example.studentmanagement.entity.Score;

import com.example.studentmanagement.repository.CourseSectionRepository;
import com.example.studentmanagement.repository.EnrollmentRepository;
import com.example.studentmanagement.repository.ScoreRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseSectionRepository sectionRepository;

    public ScoreService(
            ScoreRepository scoreRepository,
            EnrollmentRepository enrollmentRepository,
            CourseSectionRepository sectionRepository
    ) {
        this.scoreRepository = scoreRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.sectionRepository = sectionRepository;
    }

    public CourseSection updatePolicy(
            Long sectionId,
            GradingPolicyRequest request
    ) {
        int tx1 = request.tx1Weight();
        int tx2 = request.tx2Weight();
        int kthp = request.kthpWeight();

        if (tx1 + tx2 + kthp != 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tổng tỷ trọng phải bằng 100%"
            );
        }

        if (kthp <= tx1 || kthp <= tx2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tỷ trọng KTHP phải lớn hơn từng tỷ trọng TX1 và TX2"
            );
        }

        CourseSection section = lockSection(sectionId);

        boolean unchanged =
                section.getTx1Weight().equals(tx1)
                        && section.getTx2Weight().equals(tx2)
                        && section.getKthpWeight().equals(kthp);

        if (!unchanged
                && scoreRepository.countNewScoresBySection(sectionId) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Lớp đã có điểm, không được thay đổi tỷ trọng"
            );
        }

        section.setTx1Weight(tx1);
        section.setTx2Weight(tx2);
        section.setKthpWeight(kthp);
        section.setGradingConfigured(true);

        return sectionRepository.saveAndFlush(section);
    }

    @Transactional(readOnly = true)
    public List<Score> getScores(
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

        return scoreRepository.search(studentId, courseSectionId);
    }

    @Transactional(readOnly = true)
    public Score getScoreByEnrollment(Long enrollmentId) {

        checkPositiveId(enrollmentId);

        if (!enrollmentRepository.existsById(enrollmentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy đăng ký"
            );
        }

        return scoreRepository.findByEnrollmentId(enrollmentId)
                .filter(score -> score.getAverageScore() != null)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Chưa có điểm theo cấu trúc TX1, TX2, KTHP"
                ));
    }

    public Score saveScore(
            Long enrollmentId,
            ScoreRequest request
    ) {
        checkPositiveId(enrollmentId);

        Long sectionId = enrollmentRepository
                .findSectionIdByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đăng ký"
                ));

        CourseSection section = lockSection(sectionId);

        Enrollment enrollment = enrollmentRepository
                .findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy đăng ký"
                ));

        if (enrollment.getStatus() == Enrollment.Status.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể nhập điểm cho đăng ký đã hủy"
            );
        }

        if (!Boolean.TRUE.equals(section.getGradingConfigured())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phải xác nhận tỷ trọng của lớp trước khi nhập điểm"
            );
        }

        Score score = scoreRepository
                .findByEnrollmentId(enrollmentId)
                .orElseGet(Score::new);

        BigDecimal average = calculateAverage(request, section);

        score.setEnrollmentId(enrollmentId);
        score.setTx1Score(request.tx1Score());
        score.setTx2Score(request.tx2Score());
        score.setKthpScore(request.kthpScore());

        score.setAverageScore(average);
        score.setLetterGrade(calculateLetterGrade(average));

        score.setNote(trimToNull(request.note()));
        score.setUpdatedAt(LocalDateTime.now());

        return scoreRepository.saveAndFlush(score);
    }

    private BigDecimal calculateAverage(
            ScoreRequest request,
            CourseSection section
    ) {
        BigDecimal tx1 = request.tx1Score()
                .multiply(BigDecimal.valueOf(section.getTx1Weight()));

        BigDecimal tx2 = request.tx2Score()
                .multiply(BigDecimal.valueOf(section.getTx2Weight()));

        BigDecimal kthp = request.kthpScore()
                .multiply(BigDecimal.valueOf(section.getKthpWeight()));

        // Chia 100 chính xác, chỉ làm tròn một lần ở cuối.
        return tx1.add(tx2)
                .add(kthp)
                .movePointLeft(2)
                .setScale(1, RoundingMode.HALF_UP);
    }

    private String calculateLetterGrade(BigDecimal score) {

        if (score.compareTo(new BigDecimal("8.5")) >= 0) {
            return "A";
        }

        if (score.compareTo(new BigDecimal("7.7")) >= 0) {
            return "B+";
        }

        if (score.compareTo(new BigDecimal("7.0")) >= 0) {
            return "B";
        }

        if (score.compareTo(new BigDecimal("6.2")) >= 0) {
            return "C+";
        }

        if (score.compareTo(new BigDecimal("5.5")) >= 0) {
            return "C";
        }

        if (score.compareTo(new BigDecimal("4.7")) >= 0) {
            return "D+";
        }

        if (score.compareTo(new BigDecimal("4.0")) >= 0) {
            return "D";
        }

        return "F";
    }

    private CourseSection lockSection(Long sectionId) {

        checkPositiveId(sectionId);

        return sectionRepository.findByIdForUpdate(sectionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lớp học phần"
                ));
    }

    private void checkPositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID phải lớn hơn 0"
            );
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}