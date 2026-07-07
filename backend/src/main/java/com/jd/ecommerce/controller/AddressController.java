package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.entity.Address;
import com.jd.ecommerce.service.AddressService;
import com.jd.ecommerce.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "收货地址", description = "地址CRUD")
@RestController
@RequestMapping("/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Operation(summary = "获取用户地址列表")
    @GetMapping
    public Result<List<Address>> list() {
        Long userId = RequestContextUtil.getCurrentUserId();
        return Result.success(addressService.findByUserId(userId));
    }

    @Operation(summary = "添加地址")
    @PostMapping
    public Result<Void> add(@RequestBody Address address) {
        Long userId = RequestContextUtil.getCurrentUserId();
        address.setUserId(userId);
        addressService.addAddress(address);
        return Result.success();
    }

    @Operation(summary = "修改地址")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Address address) {
        Long userId = RequestContextUtil.getCurrentUserId();
        address.setId(id);
        address.setUserId(userId);
        addressService.updateAddress(address);
        return Result.success();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = RequestContextUtil.getCurrentUserId();
        addressService.deleteAddress(id, userId);
        return Result.success();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long userId = RequestContextUtil.getCurrentUserId();
        addressService.setDefault(id, userId);
        return Result.success();
    }
}
