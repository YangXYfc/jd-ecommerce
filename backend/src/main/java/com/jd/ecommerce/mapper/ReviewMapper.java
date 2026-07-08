package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.Review;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ReviewMapper {

    @Select("SELECT * FROM review WHERE id = #{id}")
    Review findById(Long id);

    @Select("SELECT * FROM review WHERE product_id = #{productId} ORDER BY created_at DESC")
    List<Review> findByProductId(Long productId);

    @Select("SELECT * FROM review WHERE order_id = #{orderId}")
    List<Review> findByOrderId(Long orderId);

    @Insert("INSERT INTO review(order_id, product_id, user_id, content, rating, images, is_anonymous) " +
            "VALUES(#{orderId}, #{productId}, #{userId}, #{content}, #{rating}, #{images}, #{isAnonymous})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Review review);

    @Update("UPDATE review SET merchant_reply = #{reply}, merchant_reply_time = #{replyTime} WHERE id = #{id}")
    int updateMerchantReply(@Param("id") Long id, @Param("reply") String reply, @Param("replyTime") java.time.LocalDateTime replyTime);

    /**
     * 查询某商家所有商品的评价（通过 product → merchant 关联）
     */
    @Select("<script>SELECT r.* FROM review r INNER JOIN product p ON r.product_id = p.id" +
            " WHERE p.merchant_id = #{merchantId}" +
            "<if test='productId != null'> AND r.product_id = #{productId}</if>" +
            "<if test='rating != null'> AND r.rating = #{rating}</if>" +
            " ORDER BY r.created_at DESC</script>")
    List<Review> findByMerchant(@Param("merchantId") Long merchantId, @Param("productId") Long productId, @Param("rating") Integer rating);
}
