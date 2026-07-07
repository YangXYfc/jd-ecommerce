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
}
