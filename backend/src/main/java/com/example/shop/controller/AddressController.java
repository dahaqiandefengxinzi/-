package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.dto.AddAddressRequest;
import com.example.shop.entity.Address;
import com.example.shop.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@Tag(name = "地址管理", description = "收货地址相关接口，需要用户登录")
@RestController
@RequestMapping("/api/address")
@SecurityRequirement(name = "Bearer Authentication")
public class AddressController {
    
    @Autowired
    private AddressService addressService;

    @Operation(summary = "获取地址列表", description = "获取当前用户的收货地址列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping
    public Result<List<Address>> getAddressList() {
        return Result.success(addressService.getAddressList());
    }

    @Operation(summary = "添加收货地址", description = "添加新的收货地址")
    @ApiResponse(responseCode = "200", description = "添加成功")
    @PostMapping
    public Result<Void> addAddress(
            @Parameter(description = "地址信息", required = true) @Valid @RequestBody AddAddressRequest request) {
        Address address = new Address();
        address.setName(request.getName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        addressService.addAddress(address);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新收货地址", description = "更新指定的收货地址")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @PutMapping("/{id}")
    public Result<Void> updateAddress(
            @Parameter(description = "地址ID", required = true) @PathVariable Long id,
            @Parameter(description = "地址信息", required = true) @Valid @RequestBody AddAddressRequest request) {
        Address address = new Address();
        address.setId(id);
        address.setName(request.getName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        addressService.updateAddress(address);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除收货地址", description = "删除指定的收货地址")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(
            @Parameter(description = "地址ID", required = true) @PathVariable Long id) {
        addressService.deleteAddress(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "设置默认地址", description = "将指定地址设为默认收货地址")
    @ApiResponse(responseCode = "200", description = "设置成功")
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultAddress(
            @Parameter(description = "地址ID", required = true) @PathVariable Long id) {
        addressService.setDefaultAddress(id);
        return Result.success("设置成功");
    }
}
