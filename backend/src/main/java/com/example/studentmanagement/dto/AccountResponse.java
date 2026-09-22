package com.example.studentmanagement.dto;

import com.example.studentmanagement.entity.UserAccount;

public record AccountResponse(
        Long id,
        String username,
        UserAccount.Role role,
        Boolean enabled
) {
    public static AccountResponse from(UserAccount account) {
        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.getRole(),
                account.getEnabled()
        );
    }
}