package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.Refund;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface RefundMapper {

    @Select("SELECT * FROM refund WHERE id = #{id}")
    Refund findById(Long id);

    @Select("SELECT * FROM refund WHERE refund_no = #{refundNo}")
    Refund findByRefundNo(String refundNo);

    @Select("SELECT * FROM refund WHERE order_id = #{orderId}")
    Refund findByOrderId(Long orderId);

    @Select("<script>SELECT * FROM refund WHERE 1=1" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            " ORDER BY created_at DESC</script>")
    List<Refund> findByConditions(@Param("userId") Long userId, @Param("merchantId") Long merchantId, @Param("status") Integer status);

    @Insert("INSERT INTO refund(refund_no, order_id, user_id, merchant_id, reason, description, amount, status, timeout_hours) " +
            "VALUES(#{refundNo}, #{orderId}, #{userId}, #{merchantId}, #{reason}, #{description}, #{amount}, #{status}, #{timeoutHours})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Refund refund);

    @Update("UPDATE refund SET status = #{status}, merchant_audit_time = #{merchantAuditTime}, merchant_remark = #{merchantRemark} WHERE id = #{id}")
    int updateMerchantAudit(@Param("id") Long id, @Param("status") Integer status, @Param("merchantAuditTime") java.time.LocalDateTime merchantAuditTime, @Param("merchantRemark") String merchantRemark);

    @Update("UPDATE refund SET status = #{status}, return_logistics_company = #{company}, return_logistics_no = #{logisticsNo}, return_ship_time = #{shipTime} WHERE id = #{id}")
    int updateReturnLogistics(@Param("id") Long id, @Param("status") Integer status, @Param("company") String company, @Param("logisticsNo") String logisticsNo, @Param("shipTime") java.time.LocalDateTime shipTime);

    @Update("UPDATE refund SET status = #{status}, merchant_confirm_time = #{confirmTime}, completed_time = #{completedTime} WHERE id = #{id}")
    int updateMerchantConfirm(@Param("id") Long id, @Param("status") Integer status, @Param("confirmTime") java.time.LocalDateTime confirmTime, @Param("completedTime") java.time.LocalDateTime completedTime);

    @Update("UPDATE refund SET status = #{status}, appeal_time = #{appealTime}, appeal_reason = #{appealReason} WHERE id = #{id}")
    int updateAppeal(@Param("id") Long id, @Param("status") Integer status, @Param("appealTime") java.time.LocalDateTime appealTime, @Param("appealReason") String appealReason);

    @Update("UPDATE refund SET status = #{status}, admin_id = #{adminId}, admin_handle_time = #{handleTime}, admin_remark = #{adminRemark}, completed_time = #{completedTime} WHERE id = #{id}")
    int updateAdminArbitrate(@Param("id") Long id, @Param("status") Integer status, @Param("adminId") Long adminId, @Param("handleTime") java.time.LocalDateTime handleTime, @Param("adminRemark") String adminRemark, @Param("completedTime") java.time.LocalDateTime completedTime);

    @Select("SELECT COUNT(*) FROM refund WHERE merchant_id = #{merchantId} AND status = #{status}")
    long countByMerchantAndStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);
}
