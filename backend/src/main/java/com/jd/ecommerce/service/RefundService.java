package com.jd.ecommerce.service;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.dto.*;
import com.jd.ecommerce.entity.*;
import com.jd.ecommerce.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class RefundService {

    @Autowired
    private RefundMapper refundMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private AdminLogMapper adminLogMapper;

    // Refund status constants
    public static final int STATUS_PENDING = 0;          // 待商家审核
    public static final int STATUS_MERCHANT_APPROVED = 1; // 商家通过
    public static final int STATUS_RETURNING = 2;         // 用户寄回中
    public static final int STATUS_COMPLETED = 3;         // 退款完成
    public static final int STATUS_MERCHANT_REJECTED = 4; // 商家拒绝
    public static final int STATUS_APPEALED = 5;          // 用户申诉
    public static final int STATUS_ADMIN_APPROVED = 6;    // 管理员支持退款
    public static final int STATUS_ADMIN_REJECTED = 7;    // 管理员拒绝退款

    @Transactional
    public Refund createRefund(Long userId, RefundCreateRequest request) {
        Order order = orderMapper.findById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人订单");
        }
        // Allow refund for orders that are paid (status >= 1 and <= 4)
        if (order.getStatus() < OrderService.STATUS_PENDING_SHIP || order.getStatus() > OrderService.STATUS_REVIEWED) {
            throw new BusinessException("当前订单状态不允许退款");
        }

        // Check if refund already exists
        Refund existing = refundMapper.findByOrderId(order.getId());
        if (existing != null) {
            throw new BusinessException("该订单已有退款申请，请勿重复申请");
        }

        Refund refund = new Refund();
        refund.setRefundNo(generateRefundNo());
        refund.setOrderId(order.getId());
        refund.setUserId(userId);
        refund.setMerchantId(order.getMerchantId());
        refund.setReason(request.getReason());
        refund.setDescription(request.getDescription());
        refund.setAmount(order.getTotalAmount());
        refund.setStatus(STATUS_PENDING);
        refund.setTimeoutHours(48);
        refundMapper.insert(refund);

        return refund;
    }

    public Refund findById(Long id) {
        Refund refund = refundMapper.findById(id);
        if (refund == null) {
            throw new BusinessException(404, "退款记录不存在");
        }
        return refund;
    }

    public Refund findByOrderId(Long orderId) {
        return refundMapper.findByOrderId(orderId);
    }

    public List<Refund> findByUser(Long userId, Integer status) {
        return refundMapper.findByConditions(userId, null, status);
    }

    public List<Refund> findByMerchant(Long merchantId, Integer status) {
        return refundMapper.findByConditions(null, merchantId, status);
    }

    public List<Refund> findAll(Integer status) {
        return refundMapper.findByConditions(null, null, status);
    }

    /**
     * Check if merchant audit has timed out
     */
    public boolean isMerchantAuditTimedOut(Refund refund) {
        if (refund.getStatus() != STATUS_PENDING) {
            return false;
        }
        LocalDateTime created = refund.getCreatedAt();
        Duration elapsed = Duration.between(created, LocalDateTime.now());
        return elapsed.toHours() >= refund.getTimeoutHours();
    }

    // === Merchant actions ===

    @Transactional
    public void merchantApprove(Long refundId, Long merchantId, RefundAuditRequest request) {
        Refund refund = findById(refundId);
        if (!refund.getMerchantId().equals(merchantId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (refund.getStatus() != STATUS_PENDING) {
            throw new BusinessException("退款状态不允许此操作");
        }
        refundMapper.updateMerchantAudit(refundId, STATUS_MERCHANT_APPROVED, LocalDateTime.now(), request.getRemark());
    }

    @Transactional
    public void merchantReject(Long refundId, Long merchantId, RefundAuditRequest request) {
        Refund refund = findById(refundId);
        if (!refund.getMerchantId().equals(merchantId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (refund.getStatus() != STATUS_PENDING) {
            throw new BusinessException("退款状态不允许此操作");
        }
        refundMapper.updateMerchantAudit(refundId, STATUS_MERCHANT_REJECTED, LocalDateTime.now(), request.getRemark());
    }

    @Transactional
    public void merchantConfirmReceive(Long refundId, Long merchantId) {
        Refund refund = findById(refundId);
        if (!refund.getMerchantId().equals(merchantId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (refund.getStatus() != STATUS_RETURNING) {
            throw new BusinessException("退款状态不允许此操作");
        }
        LocalDateTime now = LocalDateTime.now();
        refundMapper.updateMerchantConfirm(refundId, STATUS_COMPLETED, now, now);
    }

    // === User actions ===

    @Transactional
    public void submitReturnLogistics(Long refundId, Long userId, ReturnLogisticsRequest request) {
        Refund refund = findById(refundId);
        if (!refund.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (refund.getStatus() != STATUS_MERCHANT_APPROVED && refund.getStatus() != STATUS_ADMIN_APPROVED) {
            throw new BusinessException("退款状态不允许此操作");
        }
        refundMapper.updateReturnLogistics(refundId, STATUS_RETURNING,
                request.getLogisticsCompany(), request.getLogisticsNo(), LocalDateTime.now());
    }

    @Transactional
    public void appeal(Long refundId, Long userId, RefundAppealRequest request) {
        Refund refund = findById(refundId);
        if (!refund.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        // Can appeal from MERCHANT_REJECTED or from PENDING (timeout)
        if (refund.getStatus() == STATUS_MERCHANT_REJECTED) {
            refundMapper.updateAppeal(refundId, STATUS_APPEALED, LocalDateTime.now(), request.getAppealReason());
        } else if (refund.getStatus() == STATUS_PENDING && isMerchantAuditTimedOut(refund)) {
            refundMapper.updateAppeal(refundId, STATUS_APPEALED, LocalDateTime.now(), request.getAppealReason());
        } else {
            throw new BusinessException("当前退款状态不允许申诉");
        }
    }

    // === Admin actions ===

    @Transactional
    public void adminApproveRefund(Long refundId, Long adminId, RefundArbitrateRequest request) {
        Refund refund = findById(refundId);
        if (refund.getStatus() != STATUS_APPEALED) {
            throw new BusinessException("只能仲裁申诉中的退款");
        }
        LocalDateTime now = LocalDateTime.now();
        refundMapper.updateAdminArbitrate(refundId, STATUS_ADMIN_APPROVED, adminId, now, request.getRemark(), null);

        // Log
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("REFUND_ARBITRATE");
        log.setTargetType("REFUND");
        log.setTargetId(refundId);
        log.setDetail("{\"action\":\"approve\",\"remark\":\"" + request.getRemark() + "\"}");
        adminLogMapper.insert(log);
    }

    @Transactional
    public void adminRejectRefund(Long refundId, Long adminId, RefundArbitrateRequest request) {
        Refund refund = findById(refundId);
        if (refund.getStatus() != STATUS_APPEALED) {
            throw new BusinessException("只能仲裁申诉中的退款");
        }
        LocalDateTime now = LocalDateTime.now();
        refundMapper.updateAdminArbitrate(refundId, STATUS_ADMIN_REJECTED, adminId, now, request.getRemark(), null);

        // Log
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("REFUND_ARBITRATE");
        log.setTargetType("REFUND");
        log.setTargetId(refundId);
        log.setDetail("{\"action\":\"reject\",\"remark\":\"" + request.getRemark() + "\"}");
        adminLogMapper.insert(log);
    }

    private String generateRefundNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "RFD" + timestamp + random;
    }
}
