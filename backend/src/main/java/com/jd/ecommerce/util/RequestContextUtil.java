package com.jd.ecommerce.util;

import com.jd.ecommerce.common.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class RequestContextUtil {

    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException("无法获取当前请求上下文");
        }
        return attrs.getRequest();
    }

    public static Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return (Long) userId;
    }

    public static String getCurrentRole() {
        HttpServletRequest request = getCurrentRequest();
        Object role = request.getAttribute("role");
        if (role == null) {
            throw new BusinessException(401, "未登录");
        }
        return (String) role;
    }

    public static void requireRole(String... roles) {
        String currentRole = getCurrentRole();
        for (String role : roles) {
            if (role.equals(currentRole)) {
                return;
            }
        }
        throw new BusinessException(403, "无权限访问");
    }
}
