package com.jd.ecommerce.controller;

import com.jd.ecommerce.common.Result;
import com.jd.ecommerce.entity.User;
import com.jd.ecommerce.service.UserService;
import com.jd.ecommerce.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户", description = "个人信息管理")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<User> getCurrentUser() {
        Long userId = RequestContextUtil.getCurrentUserId();
        return Result.success(userService.getUserById(userId));
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/me")
    public Result<Void> updateProfile(@RequestBody User user) {
        Long userId = RequestContextUtil.getCurrentUserId();
        user.setId(userId);
        userService.updateProfile(user);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/me/password")
    public Result<Void> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        Long userId = RequestContextUtil.getCurrentUserId();
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }
}
