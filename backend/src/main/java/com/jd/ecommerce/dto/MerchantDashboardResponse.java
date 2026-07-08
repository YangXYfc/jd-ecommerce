package com.jd.ecommerce.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MerchantDashboardResponse {
    private BigDecimal totalSales;
    private long totalOrders;
    private long pendingShipOrders;
    private long pendingRefunds;
    private long todayOrders;
    private long totalProducts;
}
