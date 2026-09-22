package com.example.studentmanagement.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ScoreRequest(

        @NotNull(message = "Phải nhập điểm TX1")
        @DecimalMin(value = "0", message = "Điểm TX1 không được âm")
        @DecimalMax(value = "10", message = "Điểm TX1 không được quá 10")
        @Digits(integer = 2, fraction = 2,
                message = "Điểm TX1 có tối đa 2 chữ số thập phân")
        BigDecimal tx1Score,

        @NotNull(message = "Phải nhập điểm TX2")
        @DecimalMin(value = "0", message = "Điểm TX2 không được âm")
        @DecimalMax(value = "10", message = "Điểm TX2 không được quá 10")
        @Digits(integer = 2, fraction = 2,
                message = "Điểm TX2 có tối đa 2 chữ số thập phân")
        BigDecimal tx2Score,

        @NotNull(message = "Phải nhập điểm KTHP")
        @DecimalMin(value = "0", message = "Điểm KTHP không được âm")
        @DecimalMax(value = "10", message = "Điểm KTHP không được quá 10")
        @Digits(integer = 2, fraction = 2,
                message = "Điểm KTHP có tối đa 2 chữ số thập phân")
        BigDecimal kthpScore,

        @Size(max = 255, message = "Ghi chú không được quá 255 ký tự")
        String note

) {
}
