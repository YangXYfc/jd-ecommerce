package com.jd.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundAuditRequest {
    @NotBlank(message = "审核备注不能为空")
    private String remark;
}
