package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.dto.ReviewCreateRequest;
import com.jd.ecommerce.entity.Review;
import com.jd.ecommerce.service.MerchantService;
import com.jd.ecommerce.service.ReviewService;
import com.jd.ecommerce.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评价", description = "商品评价")
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MerchantService merchantService;

    @Operation(summary = "商品评价列表")
    @GetMapping("/product/{productId}")
    public Result<List<Review>> byProduct(@PathVariable Long productId) {
        return Result.success(reviewService.findByProductId(productId));
    }

    @Operation(summary = "创建评价(订单完成后)")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ReviewCreateRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        reviewService.createReview(userId, request);
        return Result.success();
    }

    @Operation(summary = "商家回复评价")
    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestParam String reply) {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        reviewService.merchantReply(id, merchant.getId(), reply);
        return Result.success();
    }
}
