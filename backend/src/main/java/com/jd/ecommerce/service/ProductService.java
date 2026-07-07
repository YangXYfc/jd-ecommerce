package com.jd.ecommerce.service;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.dto.ProductCreateRequest;
import com.jd.ecommerce.entity.*;
import com.jd.ecommerce.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductSkuMapper skuMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    public Product getProductDetail(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return product;
    }

    public List<ProductSku> getProductSkus(Long productId) {
        return skuMapper.findByProductId(productId);
    }

    public List<Product> searchProducts(Long categoryId, String keyword, String sortBy) {
        return productMapper.searchProducts(categoryId, keyword, null, sortBy);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productMapper.searchProducts(categoryId, null, null, null);
    }

    public List<Product> findByMerchant(Long merchantId) {
        return productMapper.findByMerchantId(merchantId);
    }

    public List<Product> findByConditions(Integer status, Long merchantId, Long categoryId) {
        return productMapper.findByConditions(status, merchantId, categoryId);
    }

    @Transactional
    public Long createProduct(ProductCreateRequest request, Long merchantId) {
        Merchant merchant = merchantMapper.findById(merchantId);
        if (merchant == null || merchant.getAuditStatus() != 1) {
            throw new BusinessException(403, "商家未通过审核，无法发布商品");
        }

        Product product = new Product();
        product.setMerchantId(merchantId);
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setSubtitle(request.getSubtitle());
        product.setMainImage(request.getMainImage());
        product.setSubImages(request.getSubImages());
        product.setDescription(request.getDescription());
        product.setDetailHtml(request.getDetailHtml());
        product.setPrice(request.getPrice());
        product.setStatus(0); // 待审核
        product.setSalesCount(0);
        productMapper.insert(product);

        // Insert SKUs
        if (request.getSkus() != null) {
            for (ProductCreateRequest.SkuRequest skuReq : request.getSkus()) {
                ProductSku sku = new ProductSku();
                sku.setProductId(product.getId());
                sku.setSkuName(skuReq.getSkuName());
                sku.setPrice(skuReq.getPrice());
                sku.setOriginalPrice(skuReq.getOriginalPrice());
                sku.setStock(skuReq.getStock());
                sku.setAttributes(skuReq.getAttributes());
                sku.setSkuImage(skuReq.getSkuImage());
                sku.setStatus(1);
                skuMapper.insert(sku);
            }
        }

        return product.getId();
    }

    @Transactional
    public void updateProduct(Product product, Long merchantId) {
        Product existing = productMapper.findById(product.getId());
        if (existing == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!existing.getMerchantId().equals(merchantId)) {
            throw new BusinessException(403, "无权修改他人商品");
        }
        productMapper.update(product);
    }

    @Transactional
    public void updateProductStatus(Long productId, Integer status, Long merchantId) {
        Product existing = productMapper.findById(productId);
        if (existing == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!existing.getMerchantId().equals(merchantId)) {
            throw new BusinessException(403, "无权操作他人商品");
        }
        // Only allow 1(上架) or 2(下架) for merchant
        if (status != 1 && status != 2) {
            throw new BusinessException("商家只能上架或下架商品");
        }
        productMapper.updateStatus(productId, status);
    }

    // Admin: audit product
    @Transactional
    public void auditProduct(Long productId, Integer status, String auditRemark) {
        productMapper.updateAudit(productId, status, auditRemark);
    }
}
