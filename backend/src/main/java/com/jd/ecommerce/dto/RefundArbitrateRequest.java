package com.jd.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundArbitrateRequest {
    @NotBlank(message = "仲裁备注不能为空")
    private String remark;
}
