package com.example.shop.controller;

import com.example.shop.service.*;
import com.example.shop.mapper.UserMapper;
import com.example.shop.entity.User;
import com.example.shop.util.JwtUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/** 隔离测试环境的真实内存投递箱；生产环境不加载该控制器。 */
@Profile("registration-test")
@RestController
@RequestMapping("/api/registration-test")
public class RegistrationTestController implements RegistrationDelivery {
    @Value("${registration.test-key}") private String key;
    @Autowired private UserMapper users;
    @Autowired @org.springframework.context.annotation.Lazy private RegistrationCodeService codes;
    @Autowired private JwtUtil jwt;
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private static class Reservation {
        String runId, code;
        volatile Map<String, Object> delivery;
        volatile Long userId;
    }
    private HttpServletRequest request() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
    private void authorize() {
        HttpServletRequest r = request();
        String supplied = r.getHeader("X-Registration-Test-Key");
        boolean local = "127.0.0.1".equals(r.getRemoteAddr()) || "::1".equals(r.getRemoteAddr()) || "0:0:0:0:0:0:0:1".equals(r.getRemoteAddr());
        if (!local || key.length() < 32 || supplied == null ||
            !MessageDigest.isEqual(key.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    private Reservation owned(String channel, String identifier, String runId) {
        authorize();
        Reservation r = reservations.get(channel + ":" + identifier);
        if (r == null || !r.runId.equals(runId)) throw new ResponseStatusException(HttpStatus.CONFLICT, "资源不属于本次运行");
        return r;
    }
    @PostMapping("/reserve")
    public synchronized Map<String, Object> reserve(@RequestBody Map<String, String> body) {
        authorize();
        String channel = body.get("channel"), identifier = codes.normalize(channel, body.get("identifier"));
        if (codes.count(channel, identifier) != 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "原固定账号已经注册");
        if (body.get("run_id") == null || !body.get("run_id").matches("[a-f0-9]{32}")) throw new IllegalArgumentException("运行标识无效");
        Reservation r = new Reservation();
        r.runId = body.get("run_id"); r.code = body.get("expected_code");
        if (r.code != null && !r.code.isEmpty() && !r.code.matches("\\d{6}")) throw new IllegalArgumentException("测试验证码格式错误");
        if (reservations.putIfAbsent(channel + ":" + identifier, r) != null) throw new ResponseStatusException(HttpStatus.CONFLICT, "账号已被其他运行预留");
        return Collections.singletonMap("unregistered", true);
    }
    public String codeFor(String channel, String identifier, String randomCode) {
        Reservation r = owned(channel, identifier, request().getHeader("X-Registration-Test-Run"));
        return r.code == null || r.code.isEmpty() ? randomCode : r.code;
    }
    public void send(String channel, String identifier, String code) {
        long start = System.nanoTime();
        Reservation r = owned(channel, identifier, request().getHeader("X-Registration-Test-Run"));
        Map<String, Object> message = new HashMap<>();
        message.put("channel", channel); message.put("purpose", "registration");
        message.put("code", code); message.put("succeeded", true);
        message.put("elapsed_ms", (System.nanoTime() - start) / 1000000);
        r.delivery = message;
    }
    public void created(String channel, String identifier, Long userId) {
        Reservation r = owned(channel, identifier, request().getHeader("X-Registration-Test-Run"));
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
                @Override public void afterCommit() { r.userId = userId; }
            });
    }
    @PostMapping("/evidence")
    public Map<String, Object> evidence(@RequestBody Map<String, String> body) {
        String channel = body.get("channel"), identifier = codes.normalize(channel, body.get("identifier"));
        Reservation r = owned(channel, identifier, body.get("run_id"));
        Map<String, Object> result = new HashMap<>();
        result.put("account_count", codes.count(channel, identifier));
        result.put("delivery", r.delivery);
        String token = body.get("token");
        boolean valid = token != null && jwt.validateToken(token);
        User user = r.userId == null ? null : users.selectById(r.userId);
        result.put("session_is_valid", valid && user != null && user.getUsername().equals(jwt.getUsernameFromToken(token)) && jwt.isTokenValid(user.getUsername(), token));
        return result;
    }
    @PostMapping("/cleanup")
    public synchronized Map<String, Object> cleanup(@RequestBody Map<String, String> body) {
        String channel = body.get("channel"), identifier = codes.normalize(channel, body.get("identifier"));
        Reservation r = owned(channel, identifier, body.get("run_id"));
        if (r.userId != null) {
            User user = users.selectById(r.userId);
            if (user != null) { jwt.invalidateToken(user.getUsername()); users.deleteById(r.userId); }
        }
        codes.cleanup(channel, identifier);
        reservations.remove(channel + ":" + identifier, r);
        return Collections.singletonMap("cleaned", true);
    }
}
