package com.example.shop.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.entity.User;
import com.example.shop.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@org.springframework.context.annotation.Profile("!registration-test")
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initAdminUser();
    }
    
    private void initAdminUser() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "admin");
        
        User existingAdmin = userMapper.selectOne(wrapper);
        
        if (existingAdmin == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("管理员");
            admin.setEmail("admin@shop.com");
            admin.setPhone("13800138000");
            admin.setStatus(1);
            admin.setRole(1);
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            userMapper.insert(admin);
            System.out.println("管理员账号初始化完成：admin / admin123");
        } else {
            existingAdmin.setPassword(passwordEncoder.encode("admin123"));
            existingAdmin.setRole(1);
            existingAdmin.setStatus(1);
            existingAdmin.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(existingAdmin);
            System.out.println("管理员账号密码已更新：admin / admin123");
        }
    }
}
