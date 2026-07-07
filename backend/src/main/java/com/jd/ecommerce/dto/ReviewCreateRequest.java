package com.jd.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewCreateRequest {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "评价内容不能为空")
    private String content;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer rating;

    private String images;
    private Integer isAnonymous;
}
