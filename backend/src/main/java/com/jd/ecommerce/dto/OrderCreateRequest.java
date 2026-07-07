package com.jd.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单请求。
 * 设计为「选中即结算」：下单时自动取当前用户购物车中 selected=1 的商品，
 * 无需前端额外传 cartIds。
 */
@Data
public class OrderCreateRequest {
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    private String remark;
}
