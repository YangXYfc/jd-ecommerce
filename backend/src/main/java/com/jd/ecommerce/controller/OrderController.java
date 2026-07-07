package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.PageResult;
import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.dto.OrderCreateRequest;
import com.jd.ecommerce.dto.ShipRequest;
import com.jd.ecommerce.entity.Order;
import com.jd.ecommerce.entity.OrderItem;
import com.jd.ecommerce.service.MerchantService;
import com.jd.ecommerce.service.OrderService;
import com.jd.ecommerce.util.RequestContextUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单", description = "订单全流程")
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MerchantService merchantService;

    @Operation(summary = "创建订单(从购物车结算)")
    @PostMapping
    public Result<Order> create(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        return Result.success(orderService.createOrder(userId, request));
    }

    @Operation(summary = "用户订单列表")
    @GetMapping
    public Result<PageResult<Order>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        Long userId = RequestContextUtil.getCurrentUserId();
        PageHelper.startPage(page, size);
        List<Order> orders = orderService.findByUser(userId, status);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.success(orderService.findById(id));
    }

    @Operation(summary = "订单明细")
    @GetMapping("/{id}/items")
    public Result<List<OrderItem>> items(@PathVariable Long id) {
        return Result.success(orderService.getOrderItems(id));
    }

    @Operation(summary = "模拟支付")
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        Long userId = RequestContextUtil.getCurrentUserId();
        orderService.payOrder(id, userId);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        Long userId = RequestContextUtil.getCurrentUserId();
        orderService.cancelOrder(id, userId, reason);
        return Result.success();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{id}/receive")
    public Result<Void> confirmReceive(@PathVariable Long id) {
        Long userId = RequestContextUtil.getCurrentUserId();
        orderService.confirmReceive(id, userId);
        return Result.success();
    }

    // === Merchant endpoints ===

    @Operation(summary = "商家订单列表")
    @GetMapping("/merchant")
    public Result<PageResult<Order>> merchantOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        PageHelper.startPage(page, size);
        List<Order> orders = orderService.findByMerchant(merchant.getId(), status);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    @Operation(summary = "商家发货")
    @PostMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id, @Valid @RequestBody ShipRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        orderService.shipOrder(id, merchant.getId(), request);
        return Result.success();
    }
}
