package com.jd.ecommerce.service;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.entity.User;
import com.jd.ecommerce.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    public void updateProfile(User user) {
        userMapper.updateProfile(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(newPassword));
    }

    // Admin methods
    public List<User> searchUsers(String keyword, String role, Integer status) {
        List<User> users = userMapper.findByConditions(keyword, role, status);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    public void updateUserStatus(Long userId, Integer status) {
        userMapper.updateStatus(userId, status);
    }

    public long countByRole(String role) {
        return userMapper.countByRole(role);
    }

    public long countAll() {
        return userMapper.countAll();
    }
}
