package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.SubjectRequest;
import com.example.studentmanagement.entity.Subject;
import com.example.studentmanagement.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Transactional(readOnly = true)
    public List<Subject> getSubjects(String keyword) {
        String text = keyword.trim();

        return subjectRepository
                .findBySubjectCodeContainingIgnoreCaseOrSubjectNameContainingIgnoreCaseOrderByIdAsc(
                        text, text
                );
    }

    @Transactional(readOnly = true)
    public Subject getSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy môn học có ID: " + id
                ));
    }

    public Subject createSubject(SubjectRequest request) {
        checkDuplicateCode(request.subjectCode(), 0L);

        Subject subject = new Subject();
        copyData(request, subject);

        return subjectRepository.saveAndFlush(subject);
    }

    public Subject updateSubject(Long id, SubjectRequest request) {
        Subject subject = getSubject(id);

        checkDuplicateCode(request.subjectCode(), id);
        copyData(request, subject);

        return subjectRepository.saveAndFlush(subject);
    }

    public void deleteSubject(Long id) {
        Subject subject = getSubject(id);

        if (subjectRepository.countCourseSectionsBySubjectId(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa môn học đã có lớp học phần"
            );
        }

        subjectRepository.delete(subject);
        subjectRepository.flush();
    }

    private void checkDuplicateCode(String subjectCode, Long currentId) {
        String code = subjectCode.trim();

        if (subjectRepository.existsBySubjectCodeAndIdNot(code, currentId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã môn đã tồn tại"
            );
        }
    }

    private void copyData(SubjectRequest request, Subject subject) {
        subject.setSubjectCode(request.subjectCode().trim());
        subject.setSubjectName(request.subjectName().trim());
        subject.setCredits(request.credits());

        String description = request.description();

        subject.setDescription(
                description == null ? null : description.trim()
        );
    }
}