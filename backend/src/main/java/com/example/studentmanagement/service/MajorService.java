package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.MajorRequest;
import com.example.studentmanagement.entity.Major;
import com.example.studentmanagement.repository.DepartmentRepository;
import com.example.studentmanagement.repository.MajorRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class MajorService {

    private final MajorRepository majorRepository;
    private final DepartmentRepository departmentRepository;

    public MajorService(
            MajorRepository majorRepository,
            DepartmentRepository departmentRepository
    ) {
        this.majorRepository = majorRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Major> getMajors(
            String keyword,
            Long departmentId
    ) {
        if (departmentId != null && departmentId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID khoa phải lớn hơn 0"
            );
        }

        String text = keyword == null ? "" : keyword.trim();

        return majorRepository.search(text, departmentId);
    }

    @Transactional(readOnly = true)
    public Major getMajor(Long id) {
        return findMajor(id);
    }

    public Major createMajor(MajorRequest request) {

        validateRequest(request, 0L);

        Major major = new Major();
        copyData(major, request);

        return majorRepository.saveAndFlush(major);
    }

    public Major updateMajor(
            Long id,
            MajorRequest request
    ) {
        Major major = findMajor(id);

        validateRequest(request, id);

        copyData(major, request);

        return majorRepository.saveAndFlush(major);
    }

    public void deleteMajor(Long id) {

        Major major = findMajor(id);

        if (majorRepository.countClasses(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa ngành đang có lớp hành chính"
            );
        }

        majorRepository.delete(major);
        majorRepository.flush();
    }

    private Major findMajor(Long id) {
        return majorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy ngành có ID: " + id
                ));
    }

    private void validateRequest(
            MajorRequest request,
            Long currentId
    ) {
        if (majorRepository.existsByMajorCodeIgnoreCaseAndIdNot(
                request.majorCode().trim(),
                currentId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã ngành đã tồn tại"
            );
        }

        if (!departmentRepository.existsById(request.departmentId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Khoa không tồn tại"
            );
        }
    }

    private void copyData(
            Major major,
            MajorRequest request
    ) {
        major.setMajorCode(request.majorCode().trim());
        major.setMajorName(request.majorName().trim());
        major.setDepartmentId(request.departmentId());
    }
}