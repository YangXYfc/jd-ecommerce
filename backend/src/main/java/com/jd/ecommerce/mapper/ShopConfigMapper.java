package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.ShopConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ShopConfigMapper {

    @Select("SELECT * FROM shop_config WHERE merchant_id = #{merchantId}")
    List<ShopConfig> findByMerchantId(Long merchantId);

    @Select("SELECT * FROM shop_config WHERE merchant_id = #{merchantId} AND config_key = #{key}")
    ShopConfig findByMerchantAndKey(@Param("merchantId") Long merchantId, @Param("key") String key);

    @Insert("INSERT INTO shop_config(merchant_id, config_key, config_value) VALUES(#{merchantId}, #{configKey}, #{configValue})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShopConfig config);

    @Update("UPDATE shop_config SET config_value = #{configValue} WHERE id = #{id}")
    int update(ShopConfig config);
}
