package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.AdminLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AdminLogMapper {

    @Select("SELECT * FROM admin_log ORDER BY created_at DESC")
    List<AdminLog> findAll();

    @Select("<script>SELECT * FROM admin_log WHERE 1=1" +
            "<if test='adminId != null'> AND admin_id = #{adminId}</if>" +
            "<if test='action != null and action != \"\"'> AND action = #{action}</if>" +
            " ORDER BY created_at DESC</script>")
    List<AdminLog> findByConditions(@Param("adminId") Long adminId, @Param("action") String action);

    @Insert("INSERT INTO admin_log(admin_id, action, target_type, target_id, detail, ip_address) " +
            "VALUES(#{adminId}, #{action}, #{targetType}, #{targetId}, #{detail}, #{ipAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdminLog log);
}
