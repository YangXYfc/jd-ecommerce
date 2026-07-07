package com.jd.ecommerce.dto;

import lombok.Data;

/**
 * 购物车项更新请求。
 * quantity 和 selected 均可选，支持部分更新：
 * - 只传 quantity → 修改数量（<=0 时删除该项）
 * - 只传 selected → 修改选中状态
 * - 同时传 → 同时更新
 */
@Data
public class CartUpdateRequest {
    private Integer quantity;
    private Integer selected;
}
