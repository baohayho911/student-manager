package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.AccountResponse;
import com.example.studentmanagement.dto.CreateAccountRequest;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.entity.UserAccount;
import com.example.studentmanagement.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@Transactional
public class AccountService {

    private final UserAccountRepository accountRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            UserAccountRepository accountRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.accountRepository = accountRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountResponse createAccount(CreateAccountRequest request) {

        if (request.username() == null
                || !request.username().matches("^[a-z0-9._-]{3,50}$")) {
            throw badRequest("Tên đăng nhập không hợp lệ");
        }

        String password = request.password();

        if (password == null
                || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw badRequest(
                    "Mật khẩu cần ít nhất 12 ký tự và không quá 72 byte UTF-8"
            );
        }

        if (request.role() == null) {
            throw badRequest("Phải chọn vai trò");
        }

        if (accountRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tên đăng nhập đã tồn tại"
            );
        }

        Student student = null;
        Teacher teacher = null;

        if (request.role() == UserAccount.Role.ADMIN) {
            if (request.profileId() != null) {
                throw badRequest("Tài khoản ADMIN không nhận profileId");
            }
        } else {
            if (request.profileId() == null || request.profileId() <= 0) {
                throw badRequest("Phải chọn hồ sơ hợp lệ");
            }

            if (request.role() == UserAccount.Role.STUDENT) {
                student = studentRepository
                        .findByIdForAccount(request.profileId())
                        .orElseThrow(() -> badRequest("Sinh viên không tồn tại"));

                if (student.getUserId() != null) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Sinh viên đã có tài khoản"
                    );
                }
            } else {
                teacher = teacherRepository
                        .findByIdForAccount(request.profileId())
                        .orElseThrow(() -> badRequest("Giảng viên không tồn tại"));

                if (teacher.getUserId() != null) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Giảng viên đã có tài khoản"
                    );
                }
            }
        }

        UserAccount account = new UserAccount();
        account.setUsername(request.username());
        account.setPassword(passwordEncoder.encode(password));
        account.setRole(request.role());
        account.setEnabled(true);

        accountRepository.saveAndFlush(account);

        if (student != null) {
            student.setUserId(account.getId());
            studentRepository.saveAndFlush(student);
        }

        if (teacher != null) {
            teacher.setUserId(account.getId());
            teacherRepository.saveAndFlush(teacher);
        }

        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getCurrentAccount(String username) {
        UserAccount account = accountRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Tài khoản không tồn tại"
                ));

        return AccountResponse.from(account);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}