package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.SemesterRequest;
import com.example.studentmanagement.entity.Semester;
import com.example.studentmanagement.repository.SemesterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public SemesterService(SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    @Transactional(readOnly = true)
    public List<Semester> getSemesters() {
        return semesterRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Semester getSemester(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy học kỳ có ID: " + id
                ));
    }

    public Semester createSemester(SemesterRequest request) {
        validateRequest(request, 0L);

        Semester semester = new Semester();
        copyData(request, semester);

        return semesterRepository.saveAndFlush(semester);
    }

    public Semester updateSemester(Long id, SemesterRequest request) {
        Semester semester = getSemester(id);

        validateRequest(request, id);
        copyData(request, semester);

        return semesterRepository.saveAndFlush(semester);
    }

    public void deleteSemester(Long id) {
        Semester semester = getSemester(id);

        if (semesterRepository.countSectionsBySemesterId(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa học kỳ đã có lớp học phần"
            );
        }

        semesterRepository.delete(semester);
        semesterRepository.flush();
    }

    private void validateRequest(SemesterRequest request, Long currentId) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ngày kết thúc không được trước ngày bắt đầu"
            );
        }

        String[] years = request.academicYear().split("-");
        int firstYear = Integer.parseInt(years[0]);
        int secondYear = Integer.parseInt(years[1]);

        if (secondYear != firstYear + 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Năm học phải gồm hai năm liên tiếp"
            );
        }

        if (semesterRepository.existsBySemesterNameAndAcademicYearAndIdNot(
                request.semesterName().trim(),
                request.academicYear(),
                currentId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Học kỳ đã tồn tại trong năm học này"
            );
        }
    }

    private void copyData(SemesterRequest request, Semester semester) {
        semester.setSemesterName(request.semesterName().trim());
        semester.setAcademicYear(request.academicYear());
        semester.setStartDate(request.startDate());
        semester.setEndDate(request.endDate());
    }
}