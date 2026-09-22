package com.example.studentmanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enrollment_id", nullable = false, unique = true)
    private Long enrollmentId;

    @Column(name = "tx1_score", precision = 4, scale = 2)
    private BigDecimal tx1Score;

    @Column(name = "tx2_score", precision = 4, scale = 2)
    private BigDecimal tx2Score;

    @Column(name = "kthp_score", precision = 4, scale = 2)
    private BigDecimal kthpScore;

    @Column(name = "total_score", precision = 4, scale = 1)
    private BigDecimal averageScore;

    @Column(name = "grade_letter", length = 2)
    private String letterGrade;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Score() {
    }

    public Long getId() {
        return id;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public BigDecimal getTx1Score() {
        return tx1Score;
    }

    public void setTx1Score(BigDecimal tx1Score) {
        this.tx1Score = tx1Score;
    }

    public BigDecimal getTx2Score() {
        return tx2Score;
    }

    public void setTx2Score(BigDecimal tx2Score) {
        this.tx2Score = tx2Score;
    }

    public BigDecimal getKthpScore() {
        return kthpScore;
    }

    public void setKthpScore(BigDecimal kthpScore) {
        this.kthpScore = kthpScore;
    }

    public BigDecimal getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(BigDecimal averageScore) {
        this.averageScore = averageScore;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
