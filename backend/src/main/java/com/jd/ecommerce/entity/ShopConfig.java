package com.jd.ecommerce.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShopConfig {
    private Long id;
    private Long merchantId;
    private String configKey;
    private String configValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
