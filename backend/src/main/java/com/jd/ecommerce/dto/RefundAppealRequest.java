package com.jd.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundAppealRequest {
    @NotBlank(message = "申诉原因不能为空")
    private String appealReason;
}
