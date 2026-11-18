package com.example.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.entity.User;
import com.example.shop.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Service
public class RegistrationCodeService {
    @Autowired private StringRedisTemplate redis;
    @Autowired private UserMapper users;
    @Autowired private RegistrationDelivery delivery;
    @Value("${registration.code-ttl-seconds:300}") private long ttl;
    @Value("${registration.resend-seconds:60}") private long resend;
    @Value("${registration.daily-limit:10}") private long daily;
    private final SecureRandom random = new SecureRandom();

    public String normalize(String channel, String identifier) {
        if (identifier == null) throw new IllegalArgumentException("请输入注册账号");
        identifier = identifier.trim();
        if ("phone".equals(channel) && identifier.matches("^1[3-9]\\d{9}$")) return identifier;
        if ("email".equals(channel) && identifier.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return identifier.toLowerCase(java.util.Locale.ROOT);
        throw new IllegalArgumentException("注册方式或账号格式错误");
    }
    public long count(String channel, String identifier) {
        return users.selectCount(new LambdaQueryWrapper<User>().eq("phone".equals(channel) ? User::getPhone : User::getEmail, identifier));
    }
    private String key(String channel, String identifier) { return "registration:" + channel + ":" + identifier; }
    public long send(String channel, String raw) {
        String identifier = normalize(channel, raw);
        if (count(channel, identifier) != 0) throw new IllegalArgumentException("账号已注册");
        String base = key(channel, identifier);
        if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(base + ":cooldown", "1", resend, TimeUnit.SECONDS)))
            throw new IllegalArgumentException("请稍后重新获取验证码");
        String day = base + ":daily:" + LocalDate.now();
        Long total = redis.execute(new DefaultRedisScript<Long>(
            "local n=redis.call('INCR',KEYS[1]); if n==1 then redis.call('EXPIRE',KEYS[1],172800) end; return n", Long.class),
            Collections.singletonList(day));
        if (total == null || total > daily) throw new IllegalArgumentException("今日验证码次数已达上限");
        String code = delivery.codeFor(channel, identifier, String.format("%06d", random.nextInt(1000000)));
        redis.opsForValue().set(base + ":code", code, ttl, TimeUnit.SECONDS);
        redis.delete(base + ":attempts");
        try { delivery.send(channel, identifier, code); }
        catch (RuntimeException e) {
            redis.delete(base + ":code"); redis.delete(base + ":cooldown");
            throw new IllegalStateException("验证码投递失败，请稍后重试");
        }
        return resend;
    }
    public String claim(String channel, String identifier, String code) {
        String base = key(channel, identifier);
        String owner = java.util.UUID.randomUUID().toString();
        if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(base + ":claim", owner, 30, TimeUnit.SECONDS)))
            throw new IllegalArgumentException("注册正在处理中");
        if (code == null || !code.matches("\\d{6}") || !code.equals(redis.opsForValue().get(base + ":code"))) {
            redis.execute(new DefaultRedisScript<Long>(
                "local n=redis.call('INCR',KEYS[1]); if n==1 then redis.call('EXPIRE',KEYS[1],ARGV[1]) end; if n>=5 then redis.call('DEL',KEYS[2]) end; return n", Long.class),
                java.util.Arrays.asList(base + ":attempts", base + ":code"), String.valueOf(ttl));
            release(channel, identifier, owner, false);
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        return owner;
    }
    public void release(String channel, String identifier, String owner, boolean success) {
        String base = key(channel, identifier);
        redis.execute(new DefaultRedisScript<Long>(
            "if redis.call('GET',KEYS[1])==ARGV[1] then if ARGV[2]=='1' then redis.call('DEL',KEYS[2]) end; return redis.call('DEL',KEYS[1]) end; return 0", Long.class),
            java.util.Arrays.asList(base + ":claim", base + ":code"), owner, success ? "1" : "0");
    }
    public void cleanup(String channel, String identifier) {
        String base = key(channel, identifier);
        redis.delete(java.util.Arrays.asList(base + ":code", base + ":claim", base + ":cooldown", base + ":attempts", base + ":daily:" + LocalDate.now()));
    }
}
