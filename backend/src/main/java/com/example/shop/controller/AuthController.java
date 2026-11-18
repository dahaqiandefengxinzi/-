package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.dto.CaptchaVerifyDTO;
import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;
import com.example.shop.entity.User;
import com.example.shop.service.CaptchaService;
import com.example.shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CaptchaService captchaService;
    @Autowired private com.example.shop.service.RegistrationCodeService registrationCodes;

    @PostMapping("/registration-code")
    public Result<Map<String, Object>> registrationCode(@RequestBody Map<String, String> request) {
        long seconds = registrationCodes.send(request.get("method"), request.get("identifier"));
        Map<String, Object> data = new HashMap<>();
        data.put("resendAfterSeconds", seconds);
        return Result.success("验证码已发送", data);
    }

    @Operation(summary = "用户注册", description = "新用户注册接口，注册成功后返回JWT Token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "注册成功"),
        @ApiResponse(responseCode = "400", description = "参数错误或用户名已存在")
    })
    @PostMapping("/register")
    public Result<Map<String, Object>> register(
            @Parameter(description = "注册信息", required = true)
            @Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        Map<String, Object> data = new HashMap<>();
        data.put("token", user.getToken());
        data.put("user", user);
        return Result.success("注册成功", data);
    }

    @Operation(summary = "用户登录", description = "用户登录接口，如果启用验证码需先验证验证码，登录成功后返回JWT Token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
        @ApiResponse(responseCode = "400", description = "验证码错误")
    })
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "登录信息", required = true)
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        if (captchaService.isEnabled()) {
            if (request.getCaptchaId() == null || request.getCaptchaId().isEmpty()) {
                return Result.error(400, "请先获取验证码");
            }
            
            CaptchaVerifyDTO verifyDTO = new CaptchaVerifyDTO();
            verifyDTO.setCaptchaId(request.getCaptchaId());
            verifyDTO.setCaptchaType(captchaService.getConfig().getCaptchaType());
            verifyDTO.setCaptchaCode(request.getCaptchaCode());
            verifyDTO.setSliderX(request.getSliderX());
            
            if (request.getClickPoints() != null && !request.getClickPoints().isEmpty()) {
                List<List<Integer>> points = new java.util.ArrayList<>();
                for (LoginRequest.ClickPoint point : request.getClickPoints()) {
                    List<Integer> p = new java.util.ArrayList<>();
                    p.add(point.getX());
                    p.add(point.getY());
                    points.add(p);
                }
                verifyDTO.setClickPoints(points);
            }
            
            String ip = getClientIp(httpRequest);
            boolean captchaValid = captchaService.verifyCaptcha(verifyDTO, ip);
            if (!captchaValid) {
                return Result.error(400, "验证码错误");
            }
        }
        
        User user = userService.login(request);
        Map<String, Object> data = new HashMap<>();
        data.put("token", user.getToken());
        data.put("user", user);
        return Result.success("登录成功", data);
    }

    @Operation(summary = "用户登出", description = "用户登出接口，清除登录状态")
    @ApiResponse(responseCode = "200", description = "登出成功")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success("登出成功");
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
