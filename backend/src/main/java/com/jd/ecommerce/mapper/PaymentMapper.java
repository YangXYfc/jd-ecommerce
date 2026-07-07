package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.Payment;
import org.apache.ibatis.annotations.*;

public interface PaymentMapper {

    @Select("SELECT * FROM payment WHERE order_id = #{orderId}")
    Payment findByOrderId(Long orderId);

    @Insert("INSERT INTO payment(payment_no, order_id, user_id, amount, pay_method, status) " +
            "VALUES(#{paymentNo}, #{orderId}, #{userId}, #{amount}, #{payMethod}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Payment payment);

    @Update("UPDATE payment SET status = #{status}, pay_time = #{payTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("payTime") java.time.LocalDateTime payTime);
}
