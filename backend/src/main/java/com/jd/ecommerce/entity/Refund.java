package com.jd.ecommerce.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Refund {
    private Long id;
    private String refundNo;
    private Long orderId;
    private Long userId;
    private Long merchantId;
    private String reason;
    private String description;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime merchantAuditTime;
    private String merchantRemark;
    private String returnLogisticsCompany;
    private String returnLogisticsNo;
    private LocalDateTime returnShipTime;
    private LocalDateTime merchantConfirmTime;
    private LocalDateTime appealTime;
    private String appealReason;
    private Long adminId;
    private LocalDateTime adminHandleTime;
    private String adminRemark;
    private LocalDateTime completedTime;
    private Integer timeoutHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
