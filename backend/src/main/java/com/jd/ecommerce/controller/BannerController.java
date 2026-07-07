package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.entity.Banner;
import com.jd.ecommerce.service.BannerService;
import com.jd.ecommerce.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "轮播图", description = "首页轮播图")
@RestController
@RequestMapping("/banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @Operation(summary = "获取轮播图列表(公开)")
    @GetMapping
    public Result<List<Banner>> list() {
        return Result.success(bannerService.findAllActive());
    }

    @Operation(summary = "管理员-轮播图列表(含隐藏)")
    @GetMapping("/all")
    public Result<List<Banner>> listAll() {
        RequestContextUtil.requireRole("ADMIN");
        return Result.success(bannerService.findAll());
    }

    @Operation(summary = "管理员-添加轮播图")
    @PostMapping
    public Result<Void> add(@RequestBody Banner banner) {
        RequestContextUtil.requireRole("ADMIN");
        bannerService.addBanner(banner);
        return Result.success();
    }

    @Operation(summary = "管理员-修改轮播图")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Banner banner) {
        RequestContextUtil.requireRole("ADMIN");
        banner.setId(id);
        bannerService.updateBanner(banner);
        return Result.success();
    }

    @Operation(summary = "管理员-删除轮播图")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        RequestContextUtil.requireRole("ADMIN");
        bannerService.deleteBanner(id);
        return Result.success();
    }
}
