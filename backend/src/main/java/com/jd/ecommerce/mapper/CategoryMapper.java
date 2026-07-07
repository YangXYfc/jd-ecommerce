package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface CategoryMapper {

    @Select("SELECT * FROM category WHERE status = 1 ORDER BY sort ASC")
    List<Category> findAllActive();

    @Select("SELECT * FROM category WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort ASC")
    List<Category> findByParentId(Long parentId);

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(Long id);

    @Insert("INSERT INTO category(name, parent_id, sort, icon, status) VALUES(#{name}, #{parentId}, #{sort}, #{icon}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);
}
