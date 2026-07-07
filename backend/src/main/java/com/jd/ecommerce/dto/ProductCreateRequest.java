package com.jd.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateRequest {
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称最长200字符")
    private String name;

    private String subtitle;
    private Long categoryId;
    private String mainImage;
    private String subImages;
    private String description;
    private String detailHtml;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    private List<SkuRequest> skus;

    @Data
    public static class SkuRequest {
        @NotBlank(message = "SKU名称不能为空")
        private String skuName;

        @NotNull
        @DecimalMin(value = "0.01")
        private BigDecimal price;

        private BigDecimal originalPrice;

        @Min(value = 0, message = "库存不能小于0")
        private Integer stock;

        private String attributes;
        private String skuImage;
    }
}
