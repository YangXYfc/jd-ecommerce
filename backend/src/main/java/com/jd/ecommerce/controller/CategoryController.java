package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.entity.Category;
import com.jd.ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "分类", description = "商品分类")
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.findAllActive());
    }

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree() {
        return Result.success(categoryService.getCategoryTree());
    }
}
