package com.example.studentmanagement.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

public final class PageSupport {

    private PageSupport() {
    }

    public static Pageable createPageable(int page, int size) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Số trang không được âm"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Kích thước trang phải từ 1 đến 100"
            );
        }

        // JPA sử dụng vị trí bắt đầu dạng số nguyên.
        if ((long) page * size > Integer.MAX_VALUE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vị trí trang vượt giới hạn hỗ trợ"
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "id")
        );
    }

    public static void checkOptionalId(Long id, String label) {
        if (id != null && id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    label + " phải lớn hơn 0"
            );
        }
    }

    public static String keywordPattern(String keyword) {

        String text = keyword == null
                ? ""
                : keyword.trim().toLowerCase(Locale.ROOT);

        // Tìm ký tự %, _ theo nghĩa thông thường nếu người dùng nhập chúng.
        text = text.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        return "%" + text + "%";
    }
}