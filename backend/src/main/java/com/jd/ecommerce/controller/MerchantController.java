package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.entity.Merchant;
import com.jd.ecommerce.entity.ShopConfig;
import com.jd.ecommerce.service.MerchantService;
import com.jd.ecommerce.util.RequestContextUtil;
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
}
