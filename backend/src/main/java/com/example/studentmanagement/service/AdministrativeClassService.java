package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.AdministrativeClassRequest;
import com.example.studentmanagement.entity.AdministrativeClass;
import com.example.studentmanagement.repository.AdministrativeClassRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class AdministrativeClassService {

    private final AdministrativeClassRepository classRepository;

    public AdministrativeClassService(
            AdministrativeClassRepository classRepository
    ) {
        this.classRepository = classRepository;
    }

    @Transactional(readOnly = true)
    public List<AdministrativeClass> getClasses(String keyword) {
        String text = keyword.trim();

        return classRepository
                .findByClassCodeContainingIgnoreCaseOrClassNameContainingIgnoreCaseOrderByIdAsc(
                        text, text
                );
    }

    @Transactional(readOnly = true)
    public AdministrativeClass getClassById(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lớp có ID: " + id
                ));
    }

    public AdministrativeClass createClass(
            AdministrativeClassRequest request
    ) {
        validateRequest(request, 0L);

        AdministrativeClass administrativeClass =
                new AdministrativeClass();

        copyData(request, administrativeClass);

        return classRepository.saveAndFlush(administrativeClass);
    }

    public AdministrativeClass updateClass(
            Long id,
            AdministrativeClassRequest request
    ) {
        AdministrativeClass administrativeClass = getClassById(id);

        validateRequest(request, id);
        copyData(request, administrativeClass);

        return classRepository.saveAndFlush(administrativeClass);
    }

    public void deleteClass(Long id) {
        AdministrativeClass administrativeClass = getClassById(id);

        if (classRepository.countStudentsByClassId(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa lớp đang có sinh viên"
            );
        }

        classRepository.delete(administrativeClass);
        classRepository.flush();
    }

    private void validateRequest(
            AdministrativeClassRequest request,
            Long currentId
    ) {
        String code = request.classCode().trim();

        if (classRepository.existsByClassCodeAndIdNot(code, currentId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã lớp đã tồn tại"
            );
        }

        if (classRepository.countMajorById(request.majorId()) == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ngành học không tồn tại"
            );
        }
    }

    private void copyData(
            AdministrativeClassRequest request,
            AdministrativeClass administrativeClass
    ) {
        administrativeClass.setClassCode(request.classCode().trim());
        administrativeClass.setClassName(request.className().trim());
        administrativeClass.setCourseYear(request.courseYear());
        administrativeClass.setMajorId(request.majorId());
    }
}