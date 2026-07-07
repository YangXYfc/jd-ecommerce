package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.dto.LoginRequest;
import com.jd.ecommerce.dto.LoginResponse;
import com.jd.ecommerce.dto.MerchantRegisterRequest;
import com.jd.ecommerce.dto.RegisterRequest;
import com.jd.ecommerce.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证", description = "注册/登录")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @Operation(summary = "商家注册(入驻申请)")
    @PostMapping("/merchant/register")
    public Result<Void> merchantRegister(@Valid @RequestBody MerchantRegisterRequest request) {
        authService.merchantRegister(request);
        return Result.success();
    }
}
