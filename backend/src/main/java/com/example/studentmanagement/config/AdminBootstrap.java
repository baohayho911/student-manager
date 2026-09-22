package com.example.studentmanagement.config;

import com.example.studentmanagement.dto.CreateAccountRequest;
import com.example.studentmanagement.entity.UserAccount;
import com.example.studentmanagement.repository.UserAccountRepository;
import com.example.studentmanagement.service.AccountService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.bootstrap-admin.enabled",
        havingValue = "true"
)
public class AdminBootstrap implements ApplicationRunner {

    private final UserAccountRepository repository;
    private final AccountService accountService;
    private final String password;

    public AdminBootstrap(
            UserAccountRepository repository,
            AccountService accountService,
            @Value("${app.bootstrap-admin.password:}") String password
    ) {
        this.repository = repository;
        this.accountService = accountService;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {

        var existing = repository.findByUsernameIgnoreCase("admin");

        if (existing.isPresent()) {
            if (existing.get().getRole() != UserAccount.Role.ADMIN) {
                throw new IllegalStateException(
                        "Tên admin đã thuộc một tài khoản không phải ADMIN"
                );
            }

            return;
        }

        accountService.createAccount(
                new CreateAccountRequest(
                        "admin",
                        password,
                        UserAccount.Role.ADMIN,
                        null
                )
        );
    }
}