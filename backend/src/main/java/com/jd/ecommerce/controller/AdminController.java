package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.common.PageResult;
import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.dto.DashboardResponse;
import com.jd.ecommerce.entity.AdminLog;
import com.jd.ecommerce.entity.Merchant;
import com.jd.ecommerce.entity.Product;
import com.jd.ecommerce.entity.User;
import com.jd.ecommerce.service.AdminService;
import com.jd.ecommerce.service.ProductService;
import com.jd.ecommerce.util.RequestContextUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员", description = "用户管理/商家审核/商品审核/退款仲裁/数据统计")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ProductService productService;

    @Operation(summary = "仪表盘数据统计")
    @GetMapping("/dashboard")
    public Result<DashboardResponse> dashboard() {
        RequestContextUtil.requireRole("ADMIN");
        return Result.success(adminService.getDashboard());
    }

    // === User management ===

    @Operation(summary = "用户列表(搜索/筛选)")
    @GetMapping("/users")
    public Result<PageResult<User>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        RequestContextUtil.requireRole("ADMIN");
        PageHelper.startPage(page, size);
        List<User> users = adminService.searchUsers(keyword, role, status);
        PageInfo<User> pageInfo = new PageInfo<>(users);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        RequestContextUtil.requireRole("ADMIN");
        Long adminId = RequestContextUtil.getCurrentUserId();
        adminService.updateUserStatus(adminId, id, status);
        return Result.success();
    }

    // === Merchant audit ===

    @Operation(summary = "商家列表")
    @GetMapping("/merchants")
    public Result<PageResult<Merchant>> merchants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Integer status) {
        RequestContextUtil.requireRole("ADMIN");
        PageHelper.startPage(page, size);
        List<Merchant> merchants = adminService.findMerchants(auditStatus, status);
        PageInfo<Merchant> pageInfo = new PageInfo<>(merchants);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    @Operation(summary = "审核商家(通过/拒绝)")
    @PostMapping("/merchants/{id}/audit")
    public Result<Void> auditMerchant(@PathVariable Long id, @RequestParam Integer auditStatus, @RequestParam(required = false) String remark) {
        RequestContextUtil.requireRole("ADMIN");
        Long adminId = RequestContextUtil.getCurrentUserId();
        adminService.auditMerchant(adminId, id, auditStatus, remark);
        return Result.success();
    }

    // === Product audit ===

    @Operation(summary = "商品列表(按状态)")
    @GetMapping("/products")
    public Result<PageResult<Product>> products(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long categoryId) {
        RequestContextUtil.requireRole("ADMIN");
        PageHelper.startPage(page, size);
        List<Product> products = productService.findByConditions(status, merchantId, categoryId);
        PageInfo<Product> pageInfo = new PageInfo<>(products);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    @Operation(summary = "审核商品(通过/拒绝)")
    @PostMapping("/products/{id}/audit")
    public Result<Void> auditProduct(@PathVariable Long id, @RequestParam Integer status, @RequestParam(required = false) String remark) {
        RequestContextUtil.requireRole("ADMIN");
        Long adminId = RequestContextUtil.getCurrentUserId();
        adminService.auditProduct(adminId, id, status, remark);
        return Result.success();
    }

    // === Admin logs ===

    @Operation(summary = "管理员操作日志")
    @GetMapping("/logs")
    public Result<PageResult<AdminLog>> logs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String action) {
        RequestContextUtil.requireRole("ADMIN");
        PageHelper.startPage(page, size);
        List<AdminLog> logs = adminService.findLogs(adminId, action);
        PageInfo<AdminLog> pageInfo = new PageInfo<>(logs);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }
}
