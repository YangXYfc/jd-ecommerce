package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface OrderItemMapper {

    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> findByOrderId(Long orderId);

    @Insert("INSERT INTO order_item(order_id, product_sku_id, product_name, sku_name, product_image, quantity, unit_price, subtotal) " +
            "VALUES(#{orderId}, #{productSkuId}, #{productName}, #{skuName}, #{productImage}, #{quantity}, #{unitPrice}, #{subtotal})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem item);

    @Insert("<script>INSERT INTO order_item(order_id, product_sku_id, product_name, sku_name, product_image, quantity, unit_price, subtotal) VALUES " +
            "<foreach collection='items' item='item' separator=','>" +
            "(#{item.orderId}, #{item.productSkuId}, #{item.productName}, #{item.skuName}, #{item.productImage}, #{item.quantity}, #{item.unitPrice}, #{item.subtotal})" +
            "</foreach></script>")
    int batchInsert(@Param("items") List<OrderItem> items);
}
