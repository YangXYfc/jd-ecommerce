package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.ProductSku;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ProductSkuMapper {

    @Select("SELECT * FROM product_sku WHERE id = #{id}")
    ProductSku findById(Long id);

    @Select("SELECT * FROM product_sku WHERE product_id = #{productId} AND status = 1")
    List<ProductSku> findByProductId(Long productId);

    @Insert("INSERT INTO product_sku(product_id, sku_name, price, original_price, stock, attributes, sku_image, status) " +
            "VALUES(#{productId}, #{skuName}, #{price}, #{originalPrice}, #{stock}, #{attributes}, #{skuImage}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductSku sku);

    @Update("UPDATE product_sku SET sku_name=#{skuName}, price=#{price}, original_price=#{originalPrice}, stock=#{stock}, attributes=#{attributes}, sku_image=#{skuImage}, status=#{status} WHERE id=#{id}")
    int update(ProductSku sku);

    @Update("UPDATE product_sku SET stock = stock - #{quantity} WHERE id = #{skuId} AND stock >= #{quantity}")
    int decreaseStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    @Update("UPDATE product_sku SET stock = stock + #{quantity} WHERE id = #{skuId}")
    int increaseStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    @Update("UPDATE product_sku SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
