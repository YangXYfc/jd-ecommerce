package com.jd.ecommerce.service;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.entity.Address;
import com.jd.ecommerce.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;

    public List<Address> findByUserId(Long userId) {
        return addressMapper.findByUserId(userId);
    }

    public Address findById(Long id) {
        Address address = addressMapper.findById(id);
        if (address == null) {
            throw new BusinessException(404, "地址不存在");
        }
        return address;
    }

    @Transactional
    public void addAddress(Address address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.clearDefault(address.getUserId());
        }
        addressMapper.insert(address);
    }

    @Transactional
    public void updateAddress(Address address) {
        Address existing = addressMapper.findById(address.getId());
        if (existing == null) {
            throw new BusinessException(404, "地址不存在");
        }
        if (!existing.getUserId().equals(address.getUserId())) {
            throw new BusinessException(403, "无权修改他人地址");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.clearDefault(address.getUserId());
        }
        addressMapper.update(address);
    }

    @Transactional
    public void deleteAddress(Long id, Long userId) {
        Address address = addressMapper.findById(id);
        if (address == null) {
            throw new BusinessException(404, "地址不存在");
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人地址");
        }
        addressMapper.delete(id);
    }

    @Transactional
    public void setDefault(Long id, Long userId) {
        Address address = addressMapper.findById(id);
        if (address == null) {
            throw new BusinessException(404, "地址不存在");
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人地址");
        }
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(id);
    }
}
