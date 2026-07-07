package com.jd.ecommerce.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Merchant {
    private Long id;
    private Long userId;
    private String shopName;
    private String shopLogo;
    private String description;
    private String contactPhone;
    private Integer status;
    private Integer auditStatus;
    private String auditRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
