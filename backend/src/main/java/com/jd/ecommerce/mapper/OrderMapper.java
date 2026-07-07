package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface OrderMapper {

    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order findById(Long id);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order findByOrderNo(String orderNo);

    @Select("<script>SELECT * FROM orders WHERE 1=1" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            " ORDER BY created_at DESC</script>")
    List<Order> findByConditions(@Param("userId") Long userId, @Param("merchantId") Long merchantId, @Param("status") Integer status);

    @Insert("INSERT INTO orders(order_no, user_id, merchant_id, total_amount, pay_amount, status, address_snapshot, remark) " +
            "VALUES(#{orderNo}, #{userId}, #{merchantId}, #{totalAmount}, #{payAmount}, #{status}, #{addressSnapshot}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Update("UPDATE orders SET status = #{status}, pay_time = #{payTime}, pay_amount = #{payAmount} WHERE id = #{id}")
    int updatePayStatus(@Param("id") Long id, @Param("status") Integer status, @Param("payTime") java.time.LocalDateTime payTime, @Param("payAmount") java.math.BigDecimal payAmount);

    @Update("UPDATE orders SET status = #{status}, ship_time = #{shipTime}, logistics_company = #{logisticsCompany}, logistics_no = #{logisticsNo} WHERE id = #{id}")
    int updateShipStatus(@Param("id") Long id, @Param("status") Integer status, @Param("shipTime") java.time.LocalDateTime shipTime, @Param("logisticsCompany") String logisticsCompany, @Param("logisticsNo") String logisticsNo);

    @Update("UPDATE orders SET status = #{status}, receive_time = #{receiveTime} WHERE id = #{id}")
    int updateReceiveStatus(@Param("id") Long id, @Param("status") Integer status, @Param("receiveTime") java.time.LocalDateTime receiveTime);

    @Update("UPDATE orders SET status = #{status}, cancel_time = #{cancelTime}, cancel_reason = #{cancelReason} WHERE id = #{id}")
    int updateCancelStatus(@Param("id") Long id, @Param("status") Integer status, @Param("cancelTime") java.time.LocalDateTime cancelTime, @Param("cancelReason") String cancelReason);

    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM orders WHERE merchant_id = #{merchantId} AND status = #{status}")
    long countByMerchantAndStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status >= 1 AND status <= 4")
    java.math.BigDecimal sumTotalSales();

    @Select("SELECT COUNT(*) FROM orders")
    long countAll();

    @Select("SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()")
    long countToday();
}
