package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.dto.CartAddRequest;
import com.jd.ecommerce.dto.CartUpdateRequest;
import com.jd.ecommerce.entity.Cart;
import com.jd.ecommerce.service.CartService;
import com.jd.ecommerce.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "购物车", description = "购物车管理")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Operation(summary = "购物车列表")
    @GetMapping
    public Result<List<Cart>> list() {
        Long userId = RequestContextUtil.getCurrentUserId();
        return Result.success(cartService.getCart(userId));
    }

    @Operation(summary = "加入购物车")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CartAddRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        cartService.addToCart(userId, request);
        return Result.success();
    }

    @Operation(summary = "修改购物车项(数量/选中)")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CartUpdateRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        cartService.updateCart(userId, id, request);
        return Result.success();
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = RequestContextUtil.getCurrentUserId();
        cartService.deleteCart(userId, id);
        return Result.success();
    }

    @Operation(summary = "全选/取消全选")
    @PutMapping("/selectAll")
    public Result<Void> selectAll(@RequestParam Integer selected) {
        Long userId = RequestContextUtil.getCurrentUserId();
        cartService.selectAll(userId, selected);
        return Result.success();
    }
}
