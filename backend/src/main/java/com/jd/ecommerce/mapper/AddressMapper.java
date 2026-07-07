package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.Address;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AddressMapper {

    @Select("SELECT * FROM address WHERE user_id = #{userId} ORDER BY is_default DESC, updated_at DESC")
    List<Address> findByUserId(Long userId);

    @Select("SELECT * FROM address WHERE id = #{id}")
    Address findById(Long id);

    @Insert("INSERT INTO address(user_id, name, phone, province, city, district, detail, is_default) " +
            "VALUES(#{userId}, #{name}, #{phone}, #{province}, #{city}, #{district}, #{detail}, #{isDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Address address);

    @Update("UPDATE address SET name=#{name}, phone=#{phone}, province=#{province}, city=#{city}, district=#{district}, detail=#{detail}, is_default=#{isDefault} WHERE id=#{id}")
    int update(Address address);

    @Delete("DELETE FROM address WHERE id = #{id}")
    int delete(Long id);

    @Update("UPDATE address SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefault(Long userId);

    @Update("UPDATE address SET is_default = 1 WHERE id = #{id}")
    int setDefault(Long id);
}
