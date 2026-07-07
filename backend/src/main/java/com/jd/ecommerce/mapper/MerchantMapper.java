package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.Merchant;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface MerchantMapper {

    @Select("SELECT * FROM merchant WHERE id = #{id}")
    Merchant findById(Long id);

    @Select("SELECT * FROM merchant WHERE user_id = #{userId}")
    Merchant findByUserId(Long userId);

    @Select("<script>SELECT * FROM merchant WHERE 1=1" +
            "<if test='auditStatus != null'> AND audit_status = #{auditStatus}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            " ORDER BY created_at DESC</script>")
    List<Merchant> findByConditions(@Param("auditStatus") Integer auditStatus, @Param("status") Integer status);

    @Insert("INSERT INTO merchant(user_id, shop_name, shop_logo, description, contact_phone, status, audit_status) " +
            "VALUES(#{userId}, #{shopName}, #{shopLogo}, #{description}, #{contactPhone}, #{status}, #{auditStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Merchant merchant);

    @Update("UPDATE merchant SET shop_name=#{shopName}, shop_logo=#{shopLogo}, description=#{description}, contact_phone=#{contactPhone} WHERE id=#{id}")
    int update(Merchant merchant);

    @Update("UPDATE merchant SET audit_status=#{auditStatus}, audit_remark=#{auditRemark} WHERE id=#{id}")
    int updateAuditStatus(@Param("id") Long id, @Param("auditStatus") Integer auditStatus, @Param("auditRemark") String auditRemark);

    @Update("UPDATE merchant SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
