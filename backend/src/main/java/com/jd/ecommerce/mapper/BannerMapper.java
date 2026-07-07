package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.Banner;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface BannerMapper {

    @Select("SELECT * FROM banner WHERE status = 1 ORDER BY sort ASC")
    List<Banner> findAllActive();

    @Select("SELECT * FROM banner ORDER BY sort ASC")
    List<Banner> findAll();

    @Insert("INSERT INTO banner(title, image_url, link_url, sort, status) VALUES(#{title}, #{imageUrl}, #{linkUrl}, #{sort}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Banner banner);

    @Update("UPDATE banner SET title=#{title}, image_url=#{imageUrl}, link_url=#{linkUrl}, sort=#{sort}, status=#{status} WHERE id=#{id}")
    int update(Banner banner);

    @Delete("DELETE FROM banner WHERE id = #{id}")
    int delete(Long id);
}
