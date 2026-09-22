package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.DepartmentRequest;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.repository.DepartmentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository
    ) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Department> getDepartments(String keyword) {

        String text = keyword == null ? "" : keyword.trim();

        return departmentRepository
                .findByDepartmentCodeContainingIgnoreCaseOrDepartmentNameContainingIgnoreCaseOrderByIdAsc(
                        text,
                        text
                );
    }

    @Transactional(readOnly = true)
    public Department getDepartment(Long id) {
        return findDepartment(id);
    }

    public Department createDepartment(DepartmentRequest request) {

        checkDuplicateCode(request.departmentCode().trim(), 0L);

        Department department = new Department();
        copyData(department, request);

        return departmentRepository.saveAndFlush(department);
    }

    public Department updateDepartment(
            Long id,
            DepartmentRequest request
    ) {
        Department department = findDepartment(id);

        checkDuplicateCode(request.departmentCode().trim(), id);

        copyData(department, request);

        return departmentRepository.saveAndFlush(department);
    }

    public void deleteDepartment(Long id) {

        Department department = findDepartment(id);

        if (departmentRepository.countMajors(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa khoa đang có ngành"
            );
        }

        if (departmentRepository.countTeachers(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa khoa đang có giảng viên"
            );
        }

        departmentRepository.delete(department);
        departmentRepository.flush();
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy khoa có ID: " + id
                ));
    }

    private void checkDuplicateCode(String code, Long currentId) {

        if (departmentRepository
                .existsByDepartmentCodeIgnoreCaseAndIdNot(code, currentId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã khoa đã tồn tại"
            );
        }
    }

    private void copyData(
            Department department,
            DepartmentRequest request
    ) {
        department.setDepartmentCode(request.departmentCode().trim());
        department.setDepartmentName(request.departmentName().trim());
    }
}
