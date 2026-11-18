package com.example.shop.service;

import com.example.shop.entity.Address;
import java.util.List;

public interface AddressService {
    List<Address> getAddressList();
    
    void addAddress(Address address);
    
    void updateAddress(Address address);
    
    void deleteAddress(Long id);
    
    void setDefaultAddress(Long id);
}
