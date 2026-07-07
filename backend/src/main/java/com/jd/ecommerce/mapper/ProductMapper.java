package com.jd.ecommerce.mapper;

import com.jd.ecommerce.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface ProductMapper {

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product findById(Long id);

    @Select("SELECT * FROM product WHERE merchant_id = #{merchantId} ORDER BY created_at DESC")
    List<Product> findByMerchantId(Long merchantId);

    @Select("<script>SELECT * FROM product WHERE status = 1" +
            "<if test='categoryId != null'> AND category_id = #{categoryId}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND name LIKE CONCAT('%',#{keyword},'%')</if>" +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            "<choose>" +
            "<when test='sortBy == \"sales\"'> ORDER BY sales_count DESC</when>" +
            "<when test='sortBy == \"price_asc\"'> ORDER BY price ASC</when>" +
            "<when test='sortBy == \"price_desc\"'> ORDER BY price DESC</when>" +
            "<otherwise> ORDER BY created_at DESC</otherwise>" +
            "</choose></script>")
    List<Product> searchProducts(@Param("categoryId") Long categoryId, @Param("keyword") String keyword,
                                  @Param("merchantId") Long merchantId, @Param("sortBy") String sortBy);

    @Select("<script>SELECT * FROM product WHERE 1=1" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='merchantId != null'> AND merchant_id = #{merchantId}</if>" +
            "<if test='categoryId != null'> AND category_id = #{categoryId}</if>" +
            " ORDER BY created_at DESC</script>")
    List<Product> findByConditions(@Param("status") Integer status, @Param("merchantId") Long merchantId, @Param("categoryId") Long categoryId);

    @Insert("INSERT INTO product(merchant_id, category_id, name, subtitle, main_image, sub_images, description, detail_html, price, status, sales_count) " +
            "VALUES(#{merchantId}, #{categoryId}, #{name}, #{subtitle}, #{mainImage}, #{subImages}, #{description}, #{detailHtml}, #{price}, #{status}, #{salesCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE product SET name=#{name}, subtitle=#{subtitle}, main_image=#{mainImage}, sub_images=#{subImages}, " +
            "description=#{description}, detail_html=#{detailHtml}, price=#{price}, category_id=#{categoryId} WHERE id=#{id}")
    int update(Product product);

    @Update("UPDATE product SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE product SET status=#{status}, audit_remark=#{auditRemark} WHERE id=#{id}")
    int updateAudit(@Param("id") Long id, @Param("status") Integer status, @Param("auditRemark") String auditRemark);

    @Update("UPDATE product SET sales_count = sales_count + #{count} WHERE id = #{id}")
    int incrementSales(@Param("id") Long id, @Param("count") int count);
}
