package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.PageResult;
import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.dto.*;
import com.jd.ecommerce.entity.Refund;
import com.jd.ecommerce.service.MerchantService;
import com.jd.ecommerce.service.RefundService;
import com.jd.ecommerce.util.RequestContextUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "退款", description = "退款全流程+异常处理")
@RestController
@RequestMapping("/refunds")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @Autowired
    private MerchantService merchantService;

    // === User endpoints ===

    @Operation(summary = "用户申请退款")
    @PostMapping
    public Result<Refund> create(@Valid @RequestBody RefundCreateRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        return Result.success(refundService.createRefund(userId, request));
    }

    @Operation(summary = "用户退款列表")
    @GetMapping
    public Result<PageResult<Refund>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        Long userId = RequestContextUtil.getCurrentUserId();
        PageHelper.startPage(page, size);
        List<Refund> refunds = refundService.findByUser(userId, status);
        PageInfo<Refund> pageInfo = new PageInfo<>(refunds);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    @Operation(summary = "退款详情")
    @GetMapping("/{id}")
    public Result<Refund> detail(@PathVariable Long id) {
        return Result.success(refundService.findById(id));
    }

    @Operation(summary = "用户填写寄回物流")
    @PostMapping("/{id}/return-logistics")
    public Result<Void> submitReturnLogistics(@PathVariable Long id, @Valid @RequestBody ReturnLogisticsRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        refundService.submitReturnLogistics(id, userId, request);
        return Result.success();
    }

    @Operation(summary = "用户申诉(商家拒绝或超时)")
    @PostMapping("/{id}/appeal")
    public Result<Void> appeal(@PathVariable Long id, @Valid @RequestBody RefundAppealRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        refundService.appeal(id, userId, request);
        return Result.success();
    }

    // === Merchant endpoints ===

    @Operation(summary = "商家退款列表")
    @GetMapping("/merchant")
    public Result<PageResult<Refund>> merchantList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        PageHelper.startPage(page, size);
        List<Refund> refunds = refundService.findByMerchant(merchant.getId(), status);
        PageInfo<Refund> pageInfo = new PageInfo<>(refunds);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    @Operation(summary = "商家审核通过")
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @Valid @RequestBody RefundAuditRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        refundService.merchantApprove(id, merchant.getId(), request);
        return Result.success();
    }

    @Operation(summary = "商家审核拒绝")
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody RefundAuditRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        refundService.merchantReject(id, merchant.getId(), request);
        return Result.success();
    }

    @Operation(summary = "商家确认收货→退款完成")
    @PostMapping("/{id}/confirm-receive")
    public Result<Void> confirmReceive(@PathVariable Long id) {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        refundService.merchantConfirmReceive(id, merchant.getId());
        return Result.success();
    }

    // === Admin endpoints ===

    @Operation(summary = "管理员退款列表(待仲裁)")
    @GetMapping("/admin")
    public Result<PageResult<Refund>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        RequestContextUtil.requireRole("ADMIN");
        PageHelper.startPage(page, size);
        List<Refund> refunds = refundService.findAll(status);
        PageInfo<Refund> pageInfo = new PageInfo<>(refunds);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    @Operation(summary = "管理员仲裁-支持退款")
    @PostMapping("/{id}/admin-approve")
    public Result<Void> adminApprove(@PathVariable Long id, @Valid @RequestBody RefundArbitrateRequest request) {
        RequestContextUtil.requireRole("ADMIN");
        Long adminId = RequestContextUtil.getCurrentUserId();
        refundService.adminApproveRefund(id, adminId, request);
        return Result.success();
    }

    @Operation(summary = "管理员仲裁-拒绝退款")
    @PostMapping("/{id}/admin-reject")
    public Result<Void> adminReject(@PathVariable Long id, @Valid @RequestBody RefundArbitrateRequest request) {
        RequestContextUtil.requireRole("ADMIN");
        Long adminId = RequestContextUtil.getCurrentUserId();
        refundService.adminRejectRefund(id, adminId, request);
        return Result.success();
    }
}
