package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.shop.entity.Address;
import com.example.shop.mapper.AddressMapper;
import com.example.shop.service.AddressService;
import com.example.shop.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    
    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> getAddressList() {
        Long userId = SecurityUtil.getCurrentUserId();
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId).orderByDesc(Address::getIsDefault);
        return addressMapper.selectList(wrapper);
    }

    @Override
    public void addAddress(Address address) {
        Long userId = SecurityUtil.getCurrentUserId();
        address.setUserId(userId);
        address.setIsDefault(0);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.insert(address);
    }

    @Override
    public void updateAddress(Address address) {
        Address existAddress = addressMapper.selectById(address.getId());
        if (existAddress == null || !existAddress.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            throw new RuntimeException("地址不存在或无权限");
        }
        
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.updateById(address);
    }

    @Override
    public void deleteAddress(Long id) {
        Address address = addressMapper.selectById(id);
        if (address != null && address.getUserId().equals(SecurityUtil.getCurrentUserId())) {
            addressMapper.deleteById(id);
        }
    }

    @Override
    public void setDefaultAddress(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        LambdaUpdateWrapper<Address> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Address::getUserId, userId).set(Address::getIsDefault, 0);
        addressMapper.update(null, updateWrapper);
        
        Address address = addressMapper.selectById(id);
        if (address != null && address.getUserId().equals(userId)) {
            address.setIsDefault(1);
            address.setUpdateTime(LocalDateTime.now());
            addressMapper.updateById(address);
        }
    }
}
