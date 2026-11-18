package com.example.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.shop.entity.User;
import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;

public interface UserService {
    User register(RegisterRequest request);
    
    User login(LoginRequest request);
    
    User getCurrentUser();
    
    void updateUserInfo(User user);
    
    void updatePassword(String oldPassword, String newPassword);
    
    void logout();
    
    IPage<User> getAdminUserList(String keyword, Integer role, Integer status, Integer page, Integer size);
    
    void adminUpdateUser(Long id, User user);
}
