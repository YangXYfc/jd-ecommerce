package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.PageResult;
import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.dto.ProductCreateRequest;
import com.jd.ecommerce.entity.Product;
import com.jd.ecommerce.entity.ProductSku;
import com.jd.ecommerce.service.MerchantService;
import com.jd.ecommerce.service.ProductService;
import com.jd.ecommerce.util.RequestContextUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品", description = "商品浏览与管理")
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private MerchantService merchantService;

    @Operation(summary = "商品列表(分页/搜索/分类/排序)")
    @GetMapping("/list")
    public Result<PageResult<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "created") String sortBy) {
        PageHelper.startPage(page, size);
        List<Product> products = productService.searchProducts(categoryId, keyword, sortBy);
        PageInfo<Product> pageInfo = new PageInfo<>(products);
        return Result.success(new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize()));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @Operation(summary = "获取商品SKU列表")
    @GetMapping("/{id}/skus")
    public Result<List<ProductSku>> skus(@PathVariable Long id) {
        return Result.success(productService.getProductSkus(id));
    }

    @Operation(summary = "按分类获取商品")
    @GetMapping("/category/{categoryId}")
    public Result<List<Product>> byCategory(@PathVariable Long categoryId) {
        return Result.success(productService.getProductsByCategory(categoryId));
    }

    // === Merchant endpoints ===

    @Operation(summary = "商家发布商品")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProductCreateRequest request) {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        return Result.success(productService.createProduct(request, merchant.getId()));
    }

    @Operation(summary = "商家修改商品")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product) {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        product.setId(id);
        productService.updateProduct(product, merchant.getId());
        return Result.success();
    }

    @Operation(summary = "商家上下架商品")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        productService.updateProductStatus(id, status, merchant.getId());
        return Result.success();
    }

    @Operation(summary = "商家商品列表")
    @GetMapping("/merchant")
    public Result<List<Product>> merchantProducts() {
        Long userId = RequestContextUtil.getCurrentUserId();
        com.jd.ecommerce.entity.Merchant merchant = merchantService.findByUserId(userId);
        if (merchant == null) {
            throw new com.jd.ecommerce.common.BusinessException(403, "非商家账号");
        }
        return Result.success(productService.findByMerchant(merchant.getId()));
    }
}
