package com.jd.ecommerce.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardResponse {
    private long totalSales;
    private long totalOrders;
    private long totalUsers;
    private long todayOrders;
    private List<Map<String, Object>> recentOrders;
}
