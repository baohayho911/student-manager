package com.example.studentmanagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GradingPolicyRequest(

        @NotNull(message = "Phải nhập tỷ trọng TX1")
        @Min(value = 15, message = "Tỷ trọng TX1 phải ít nhất 15%")
        @Max(value = 100, message = "Tỷ trọng TX1 không được quá 100%")
        Integer tx1Weight,

        @NotNull(message = "Phải nhập tỷ trọng TX2")
        @Min(value = 15, message = "Tỷ trọng TX2 phải ít nhất 15%")
        @Max(value = 100, message = "Tỷ trọng TX2 không được quá 100%")
        Integer tx2Weight,

        @NotNull(message = "Phải nhập tỷ trọng KTHP")
        @Min(value = 1, message = "Tỷ trọng KTHP phải lớn hơn 0")
        @Max(value = 100, message = "Tỷ trọng KTHP không được quá 100%")
        Integer kthpWeight

) {
}
