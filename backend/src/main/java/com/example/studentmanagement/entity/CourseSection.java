package com.example.studentmanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "course_sections")
public class CourseSection {

    public enum Status {
        OPEN,
        CLOSED,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_code", nullable = false, length = 30)
    private String sectionCode;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "room", length = 50)
    private String room;

    @Column(name = "schedule", length = 100)
    private String schedule;

    @Column(name = "maximum_students", nullable = false)
    private Integer maximumStudents;

    @Column(name = "tx1_weight", nullable = false)
    private Integer tx1Weight = 20;

    @Column(name = "tx2_weight", nullable = false)
    private Integer tx2Weight = 20;

    @Column(name = "kthp_weight", nullable = false)
    private Integer kthpWeight = 60;

    @Column(name = "grading_configured", nullable = false)
    private Boolean gradingConfigured = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    public CourseSection() {
    }

    public Long getId() {
        return id;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Integer getMaximumStudents() {
        return maximumStudents;
    }

    public void setMaximumStudents(Integer maximumStudents) {
        this.maximumStudents = maximumStudents;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getTx1Weight() {
        return tx1Weight;
    }

    public void setTx1Weight(Integer tx1Weight) {
        this.tx1Weight = tx1Weight;
    }

    public Integer getTx2Weight() {
        return tx2Weight;
    }

    public void setTx2Weight(Integer tx2Weight) {
        this.tx2Weight = tx2Weight;
    }

    public Integer getKthpWeight() {
        return kthpWeight;
    }

    public void setKthpWeight(Integer kthpWeight) {
        this.kthpWeight = kthpWeight;
    }

    public Boolean getGradingConfigured() {
        return gradingConfigured;
    }

    public void setGradingConfigured(Boolean gradingConfigured) {
        this.gradingConfigured = gradingConfigured;
    }

    public boolean isGradingConfigured() {
        return gradingConfigured;
    }
}
