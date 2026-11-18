package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.dto.CaptchaConfigDTO;
import com.example.shop.dto.CaptchaVerifyDTO;
import com.example.shop.entity.CaptchaConfig;
import com.example.shop.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "验证码管理", description = "验证码生成和验证接口")
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    
    @Autowired
    private CaptchaService captchaService;

    @Operation(summary = "检查验证码是否启用", description = "检查系统是否启用了验证码功能")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/enabled")
    public Result<Map<String, Object>> checkEnabled() {
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", captchaService.isEnabled());
        CaptchaConfig config = captchaService.getConfig();
        if (config != null) {
            data.put("captchaType", config.getCaptchaType());
        }
        return Result.success(data);
    }

    @Operation(summary = "生成验证码", description = "根据系统配置生成对应类型的验证码")
    @ApiResponse(responseCode = "200", description = "生成成功")
    @GetMapping("/generate")
    public Result<Map<String, Object>> generateCaptcha(
            @Parameter(description = "验证码类型（可选）") @RequestParam(required = false) String type,
            HttpServletRequest request) {
        String ip = getClientIp(request);
        captchaService.checkIpLimit(ip);
        Map<String, Object> captcha = captchaService.generateCaptcha(ip, type);
        return Result.success(captcha);
    }

    @Operation(summary = "验证验证码", description = "验证用户输入的验证码是否正确")
    @ApiResponse(responseCode = "200", description = "验证完成")
    @PostMapping("/verify")
    public Result<Map<String, Object>> verifyCaptcha(
            @Parameter(description = "验证信息", required = true) @Valid @RequestBody CaptchaVerifyDTO dto,
            HttpServletRequest request) {
        String ip = getClientIp(request);
        boolean success = captchaService.verifyCaptcha(dto, ip);
        Map<String, Object> data = new HashMap<>();
        data.put("success", success);
        if (success) {
            return Result.success("验证成功", data);
        } else {
            // 验证失败也返回 200，通过 success 字段区分结果
            return Result.success("验证码错误", data);
        }
    }

    @Operation(summary = "获取验证码配置", description = "管理员获取验证码配置信息")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/config")
    public Result<CaptchaConfig> getConfig() {
        return Result.success(captchaService.getConfig());
    }

    @Operation(summary = "更新验证码配置", description = "管理员更新验证码配置")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/config")
    public Result<Void> updateConfig(
            @Parameter(description = "配置信息", required = true) @Valid @RequestBody CaptchaConfigDTO dto) {
        captchaService.updateConfig(dto);
        return Result.success("配置更新成功");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
