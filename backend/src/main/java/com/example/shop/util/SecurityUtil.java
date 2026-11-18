package com.example.shop.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.entity.User;
import com.example.shop.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    
    private static UserMapper userMapper;
    
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        SecurityUtil.userMapper = userMapper;
    }
    
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
    
    public static Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username != null) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, username);
            User user = userMapper.selectOne(wrapper);
            if (user != null) {
                return user.getId();
            }
        }
        return null;
    }
}
