package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.dto.UpdatePasswordRequest;
import com.example.shop.entity.User;
import com.example.shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@Tag(name = "用户管理", description = "用户信息相关接口，需要用户登录")
@RestController
@RequestMapping("/api/user")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        User user = userService.getCurrentUser();
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(
            @Parameter(description = "用户信息", required = true) @RequestBody User user) {
        userService.updateUserInfo(user);
        return Result.success("更新成功");
    }

    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @ApiResponse(responseCode = "200", description = "修改成功")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @Parameter(description = "密码信息", required = true) @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功");
    }
}
