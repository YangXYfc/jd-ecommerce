package com.jd.ecommerce.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private String subImages;
    private String description;
    private String detailHtml;
    private BigDecimal price;
    private Integer status;
    private String auditRemark;
    private Integer salesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
