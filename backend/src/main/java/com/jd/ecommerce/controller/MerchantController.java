package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.common.PageResult;
import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.dto.MerchantDashboardResponse;
import com.jd.ecommerce.entity.Merchant;
import com.jd.ecommerce.entity.Order;
import com.jd.ecommerce.entity.Product;
import com.jd.ecommerce.entity.Refund;
import com.jd.ecommerce.entity.Review;
import com.jd.ecommerce.entity.ShopConfig;
import com.jd.ecommerce.service.MerchantService;
import com.jd.ecommerce.service.OrderService;
import com.jd.ecommerce.service.ProductService;
import com.jd.ecommerce.service.RefundService;
import com.jd.ecommerce.service.ReviewService;
import com.jd.ecommerce.util.RequestContextUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商家", description = "店铺管理")
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RefundService refundService;

    @Autowired
    private ReviewService reviewService;

    @Operation(summary = "获取当前商家信息")
    @GetMapping("/info")
    public Result<Merchant> info() {
        Long userId = RequestContextUtil.getCurrentUserId();
        Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new BusinessException(403, "非商家账号");
        }
        return Result.success(merchant);
    }

    @Operation(summary = "修改店铺信息")
    @PutMapping("/info")
    public Result<Void> update(@RequestBody Merchant merchant) {
        Long userId = RequestContextUtil.getCurrentUserId();
        Merchant existing = merchantService.findByUserId(userId);
        if (existing == null) {
            throw new BusinessException(403, "非商家账号");
        }
        merchant.setId(existing.getId());
        merchantService.updateMerchant(merchant);
        return Result.success();
    }

    @Operation(summary = "获取店铺配置")
    @GetMapping("/configs")
    public Result<List<ShopConfig>> configs() {
        Long userId = RequestContextUtil.getCurrentUserId();
        Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new BusinessException(403, "非商家账号");
        }
        return Result.success(merchantService.getShopConfigs(merchant.getId()));
    }

    @Operation(summary = "保存店铺配置")
    @PostMapping("/configs")
    public Result<Void> saveConfig(@RequestBody ShopConfig config) {
        Long userId = RequestContextUtil.getCurrentUserId();
        Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new BusinessException(403, "非商家账号");
        }
        config.setMerchantId(merchant.getId());
        merchantService.saveShopConfig(config);
        return Result.success();
    }

    // ==================== 商品管理 ====================

    @Operation(summary = "商家商品列表(分页)")
    @GetMapping("/products")
    public Result<PageResult<Product>> products(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId) {
        Merchant merchant = resolveCurrentMerchant();
        PageHelper.startPage(page, size);
        List<Product> products = productService.findByConditions(status, merchant.getId(), categoryId);
        PageInfo<Product> pageInfo = new PageInfo<>(products);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    // ==================== 订单管理 ====================

    @Operation(summary = "商家订单列表(分页)")
    @GetMapping("/orders")
    public Result<PageResult<Order>> orders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        Merchant merchant = resolveCurrentMerchant();
        PageHelper.startPage(page, size);
        List<Order> orders = orderService.findByMerchant(merchant.getId(), status);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    @Operation(summary = "商家订单详情")
    @GetMapping("/orders/{id}")
    public Result<Order> orderDetail(@PathVariable Long id) {
        Merchant merchant = resolveCurrentMerchant();
        Order order = orderService.findById(id);
        if (!order.getMerchantId().equals(merchant.getId())) {
            throw new BusinessException(403, "无权查看其他商家的订单");
        }
        return Result.success(order);
    }

    // ==================== 退款管理 ====================

    @Operation(summary = "商家退款列表(分页)")
    @GetMapping("/refunds")
    public Result<PageResult<Refund>> refunds(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status) {
        Merchant merchant = resolveCurrentMerchant();
        PageHelper.startPage(page, size);
        List<Refund> refunds = refundService.findByMerchant(merchant.getId(), status);
        PageInfo<Refund> pageInfo = new PageInfo<>(refunds);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    // ==================== 评价管理 ====================

    @Operation(summary = "商家评价列表(分页)")
    @GetMapping("/reviews")
    public Result<PageResult<Review>> reviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer rating) {
        Merchant merchant = resolveCurrentMerchant();
        PageHelper.startPage(page, size);
        List<Review> reviews = reviewService.findByMerchant(merchant.getId(), productId, rating);
        PageInfo<Review> pageInfo = new PageInfo<>(reviews);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    // ==================== 仪表盘统计 ====================

    @Operation(summary = "商家仪表盘统计")
    @GetMapping("/dashboard")
    public Result<MerchantDashboardResponse> dashboard() {
        Merchant merchant = resolveCurrentMerchant();
        return Result.success(merchantService.getMerchantDashboard(merchant.getId()));
    }

    // ==================== 辅助方法 ====================

    /**
     * 解析当前登录用户对应的商家身份，非商家则抛 403
     */
    private Merchant resolveCurrentMerchant() {
        Long userId = RequestContextUtil.getCurrentUserId();
        Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new BusinessException(403, "非商家账号");
        }
        return merchant;
    }
}
