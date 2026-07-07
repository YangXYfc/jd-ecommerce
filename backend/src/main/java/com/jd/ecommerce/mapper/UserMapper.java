package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User findByPhone(String phone);

    @Select("<script>SELECT * FROM user WHERE 1=1" +
            "<if test='keyword != null and keyword != \"\"'> AND (username LIKE CONCAT('%',#{keyword},'%') OR phone LIKE CONCAT('%',#{keyword},'%') OR nickname LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='role != null and role != \"\"'> AND role = #{role}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            " ORDER BY created_at DESC</script>")
    List<User> findByConditions(@Param("keyword") String keyword, @Param("role") String role, @Param("status") Integer status);

    @Insert("INSERT INTO user(username, password, phone, email, nickname, gender, status, role) " +
            "VALUES(#{username}, #{password}, #{phone}, #{email}, #{nickname}, #{gender}, #{status}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET nickname=#{nickname}, gender=#{gender}, avatar=#{avatar}, email=#{email}, phone=#{phone} WHERE id=#{id}")
    int updateProfile(User user);

    @Update("UPDATE user SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE user SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM user WHERE role = #{role}")
    long countByRole(String role);

    @Select("SELECT COUNT(*) FROM user")
    long countAll();
}
