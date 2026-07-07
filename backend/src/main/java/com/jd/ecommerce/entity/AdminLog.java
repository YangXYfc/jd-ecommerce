package com.jd.ecommerce.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminLog {
    private Long id;
    private Long adminId;
    private String action;
    private String targetType;
    private Long targetId;
    private String detail;
    private String ipAddress;
    private LocalDateTime createdAt;
}
