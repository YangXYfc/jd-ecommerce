package com.jd.ecommerce.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductSku {
    private Long id;
    private Long productId;
    private String skuName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String attributes;
    private String skuImage;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
