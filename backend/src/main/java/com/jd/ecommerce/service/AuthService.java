package com.jd.ecommerce.service;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.dto.LoginRequest;
import com.jd.ecommerce.dto.LoginResponse;
import com.jd.ecommerce.dto.MerchantRegisterRequest;
import com.jd.ecommerce.dto.RegisterRequest;
import com.jd.ecommerce.entity.Merchant;
import com.jd.ecommerce.entity.User;
import com.jd.ecommerce.mapper.MerchantMapper;
import com.jd.ecommerce.mapper.UserMapper;
import com.jd.ecommerce.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setRole(user.getRole());
        return resp;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        if (request.getPhone() != null && userMapper.findByPhone(request.getPhone()) != null) {
            throw new BusinessException("手机号已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setGender(0);
        user.setStatus(1);
        user.setRole("USER");
        userMapper.insert(user);
    }

    @Transactional
    public void merchantRegister(MerchantRegisterRequest request) {
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // Create user with MERCHANT role
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getContactPhone());
        user.setNickname(request.getShopName());
        user.setGender(0);
        user.setStatus(1);
        user.setRole("MERCHANT");
        userMapper.insert(user);

        // Create merchant record (pending audit)
        Merchant merchant = new Merchant();
        merchant.setUserId(user.getId());
        merchant.setShopName(request.getShopName());
        merchant.setDescription(request.getDescription());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setStatus(0); // 停业 until approved
        merchant.setAuditStatus(0); // 待审核
        merchantMapper.insert(merchant);
    }
}
