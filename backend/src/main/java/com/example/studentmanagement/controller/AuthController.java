package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.AccountResponse;
import com.example.studentmanagement.service.AccountService;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of(
                "headerName", token.getHeaderName(),
                "token", token.getToken()
        );
    }

    @GetMapping("/me")
    public AccountResponse me(Principal principal) {
        return accountService.getCurrentAccount(principal.getName());
    }
}