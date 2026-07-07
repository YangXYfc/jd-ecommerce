package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.Cart;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface CartMapper {

    @Select("SELECT * FROM cart WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<Cart> findByUserId(Long userId);

    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND selected = 1")
    List<Cart> findSelectedByUserId(Long userId);

    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND product_sku_id = #{skuId}")
    Cart findByUserAndSku(@Param("userId") Long userId, @Param("skuId") Long skuId);

    @Select("SELECT * FROM cart WHERE id = #{id} AND user_id = #{userId}")
    Cart findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("INSERT INTO cart(user_id, product_sku_id, quantity, selected) VALUES(#{userId}, #{productSkuId}, #{quantity}, #{selected})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Cart cart);

    @Update("UPDATE cart SET quantity = #{quantity} WHERE id = #{id} AND user_id = #{userId}")
    int updateQuantity(@Param("id") Long id, @Param("userId") Long userId, @Param("quantity") int quantity);

    @Update("UPDATE cart SET selected = #{selected} WHERE id = #{id} AND user_id = #{userId}")
    int updateSelected(@Param("id") Long id, @Param("userId") Long userId, @Param("selected") int selected);

    @Update("UPDATE cart SET selected = #{selected} WHERE user_id = #{userId}")
    int updateAllSelected(@Param("userId") Long userId, @Param("selected") int selected);

    @Delete("DELETE FROM cart WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM cart WHERE user_id = #{userId} AND selected = 1")
    int deleteSelectedByUserId(Long userId);
}
