package com.jd.ecommerce.service;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.entity.AdminLog;
import com.jd.ecommerce.entity.Merchant;
import com.jd.ecommerce.entity.User;
import com.jd.ecommerce.mapper.AdminLogMapper;
import com.jd.ecommerce.mapper.MerchantMapper;
import com.jd.ecommerce.mapper.UserMapper;
import com.jd.ecommerce.dto.DashboardResponse;
import com.jd.ecommerce.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private AdminLogMapper adminLogMapper;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserService userService;

    // === User management ===

    public List<User> searchUsers(String keyword, String role, Integer status) {
        return userService.searchUsers(keyword, role, status);
    }

    @Transactional
    public void updateUserStatus(Long adminId, Long userId, Integer status) {
        userService.updateUserStatus(userId, status);

        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("USER_DISABLE");
        log.setTargetType("USER");
        log.setTargetId(userId);
        log.setDetail("{\"status\":" + status + "}");
        adminLogMapper.insert(log);
    }

    // === Merchant audit ===

    public List<Merchant> findMerchants(Integer auditStatus, Integer status) {
        return merchantMapper.findByConditions(auditStatus, status);
    }

    @Transactional
    public void auditMerchant(Long adminId, Long merchantId, Integer auditStatus, String remark) {
        Merchant merchant = merchantMapper.findById(merchantId);
        if (merchant == null) {
            throw new BusinessException(404, "商家不存在");
        }

        merchantMapper.updateAuditStatus(merchantId, auditStatus, remark);

        // If approved, set merchant status to active (1)
        if (auditStatus == 1) {
            merchantMapper.updateStatus(merchantId, 1);
            // Also update user role to MERCHANT if not already
            User user = userMapper.findById(merchant.getUserId());
            if (user != null && !"MERCHANT".equals(user.getRole())) {
                // Role already set at registration, but ensure
            }
        }

        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("MERCHANT_AUDIT");
        log.setTargetType("MERCHANT");
        log.setTargetId(merchantId);
        log.setDetail("{\"auditStatus\":" + auditStatus + ",\"remark\":\"" + remark + "\"}");
        adminLogMapper.insert(log);
    }

    // === Product audit ===

    @Transactional
    public void auditProduct(Long adminId, Long productId, Integer status, String remark) {
        productService.auditProduct(productId, status, remark);

        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("PRODUCT_AUDIT");
        log.setTargetType("PRODUCT");
        log.setTargetId(productId);
        log.setDetail("{\"status\":" + status + ",\"remark\":\"" + remark + "\"}");
        adminLogMapper.insert(log);
    }

    // === Admin logs ===

    public List<AdminLog> findLogs(Long adminId, String action) {
        return adminLogMapper.findByConditions(adminId, action);
    }

    // === Dashboard ===

    public DashboardResponse getDashboard() {
        DashboardResponse resp = new DashboardResponse();
        resp.setTotalSales(orderMapper.sumTotalSales().longValue());
        resp.setTotalOrders(orderMapper.countAll());
        resp.setTotalUsers(userMapper.countAll());
        resp.setTodayOrders(orderMapper.countToday());
        return resp;
    }
}
