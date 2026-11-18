package com.example.shop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.entity.User;
import com.example.shop.mapper.UserMapper;
import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;
import com.example.shop.service.UserService;
import com.example.shop.util.JwtUtil;
import com.example.shop.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired private com.example.shop.service.RegistrationCodeService codes;
    @Autowired private com.example.shop.service.RegistrationDelivery delivery;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public User register(RegisterRequest request) {
        final String channel = request.getMethod();
        final boolean verified = channel != null;
        final String identifier = verified ? codes.normalize(channel, "phone".equals(channel) ? request.getPhone() : request.getEmail()) : null;
        if (verified) {
            if (codes.count(channel, identifier) != 0) throw new IllegalArgumentException("账号已注册");
            request.setUsername("u_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 18));
            request.setPhone("phone".equals(channel) ? identifier : null);
            request.setEmail("email".equals(channel) ? identifier : null);
            final String owner = codes.claim(channel, identifier, request.getCode());
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void afterCompletion(int status) {
                        codes.release(channel, identifier, owner, status == STATUS_COMMITTED);
                    }
                });
        } else {
            if (request.getUsername() == null || !request.getUsername().matches(".{3,20}"))
                throw new IllegalArgumentException("用户名长度为3-20个字符");
            if (request.getPhone() != null && !request.getPhone().isEmpty()) request.setPhone(codes.normalize("phone", request.getPhone()));
            else request.setPhone(null);
            if (request.getEmail() != null && !request.getEmail().isEmpty()) request.setEmail(codes.normalize("email", request.getEmail()));
            else request.setEmail(null);
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        BeanUtil.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setRole(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        if (verified) delivery.created(channel, identifier, user.getId());
        
        user.setToken(jwtUtil.generateToken(user.getUsername(), 0));
        return user;
    }

    @Override
    public User login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        user.setToken(jwtUtil.generateToken(user.getUsername(), user.getRole() != null ? user.getRole() : 0));
        return user;
    }

    @Override
    public User getCurrentUser() {
        String username = SecurityUtil.getCurrentUsername();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public void updateUserInfo(User user) {
        User currentUser = getCurrentUser();
        currentUser.setNickname(user.getNickname());
        currentUser.setAvatar(user.getAvatar());
        currentUser.setGender(user.getGender());
        currentUser.setBirthday(user.getBirthday());
        currentUser.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(currentUser);
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        User currentUser = getCurrentUser();
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        currentUser.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(currentUser);
    }

    @Override
    public void logout() {
        String username = SecurityUtil.getCurrentUsername();
        // 如果用户名为空（token已过期或无效），静默处理，不抛出异常
        if (username != null) {
            jwtUtil.invalidateToken(username);
        }
    }
    
    @Override
    public IPage<User> getAdminUserList(String keyword, Integer role, Integer status, Integer page, Integer size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        
        IPage<User> userPage = userMapper.selectPage(pageParam, wrapper);
        
        for (User user : userPage.getRecords()) {
            user.setPassword(null);
        }
        
        return userPage;
    }
    
    @Override
    public void adminUpdateUser(Long id, User user) {
        User existUser = userMapper.selectById(id);
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getNickname() != null) {
            existUser.setNickname(user.getNickname());
        }
        if (user.getEmail() != null) {
            existUser.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            existUser.setPhone(user.getPhone());
        }
        if (user.getRole() != null) {
            existUser.setRole(user.getRole());
        }
        if (user.getStatus() != null) {
            existUser.setStatus(user.getStatus());
        }
        
        existUser.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(existUser);
    }
}
