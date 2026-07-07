package com.jd.ecommerce.service;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.dto.CartAddRequest;
import com.jd.ecommerce.dto.CartUpdateRequest;
import com.jd.ecommerce.entity.Cart;
import com.jd.ecommerce.entity.ProductSku;
import com.jd.ecommerce.mapper.CartMapper;
import com.jd.ecommerce.mapper.ProductSkuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductSkuMapper skuMapper;

    public List<Cart> getCart(Long userId) {
        return cartMapper.findByUserId(userId);
    }

    public void addToCart(Long userId, CartAddRequest request) {
        ProductSku sku = skuMapper.findById(request.getProductSkuId());
        if (sku == null) {
            throw new BusinessException(404, "SKU不存在");
        }
        if (sku.getStock() < request.getQuantity()) {
            throw new BusinessException("库存不足");
        }

        Cart existing = cartMapper.findByUserAndSku(userId, request.getProductSkuId());
        if (existing != null) {
            cartMapper.updateQuantity(existing.getId(), userId, existing.getQuantity() + request.getQuantity());
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductSkuId(request.getProductSkuId());
            cart.setQuantity(request.getQuantity());
            cart.setSelected(1);
            cartMapper.insert(cart);
        }
    }

    /**
     * 修改购物车项（数量/选中状态）。
     * 校验 cartId 归属当前用户，防止越权操作。
     */
    public void updateCart(Long userId, Long cartId, CartUpdateRequest request) {
        Cart target = cartMapper.findByIdAndUserId(cartId, userId);
        if (target == null) {
            throw new BusinessException(404, "购物车项不存在");
        }
        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                cartMapper.delete(cartId, userId);
                return;
            }
            cartMapper.updateQuantity(cartId, userId, request.getQuantity());
        }
        if (request.getSelected() != null) {
            cartMapper.updateSelected(cartId, userId, request.getSelected());
        }
    }

    /**
     * 删除购物车项。
     * 校验 cartId 归属当前用户，防止越权删除。
     */
    public void deleteCart(Long userId, Long cartId) {
        Cart target = cartMapper.findByIdAndUserId(cartId, userId);
        if (target == null) {
            throw new BusinessException(404, "购物车项不存在");
        }
        cartMapper.delete(cartId, userId);
    }

    public void selectAll(Long userId, Integer selected) {
        cartMapper.updateAllSelected(userId, selected);
    }

    public List<Cart> getSelectedItems(Long userId) {
        return cartMapper.findSelectedByUserId(userId);
    }

    public void clearSelected(Long userId) {
        cartMapper.deleteSelectedByUserId(userId);
    }
}
