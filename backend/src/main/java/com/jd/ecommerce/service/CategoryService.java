package com.jd.ecommerce.service;

import com.jd.ecommerce.entity.Category;
import com.jd.ecommerce.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> findAllActive() {
        return categoryMapper.findAllActive();
    }

    /**
     * Build category tree (top-level → children)
     */
    public List<Map<String, Object>> getCategoryTree() {
        List<Category> all = categoryMapper.findAllActive();
        Map<Long, List<Category>> byParent = all.stream()
                .collect(Collectors.groupingBy(Category::getParentId));

        return all.stream()
                .filter(c -> c.getParentId() == 0)
                .map(c -> {
                    Map<String, Object> node = new java.util.LinkedHashMap<>();
                    node.put("id", c.getId());
                    node.put("name", c.getName());
                    node.put("icon", c.getIcon());
                    node.put("sort", c.getSort());
                    node.put("children", buildChildren(c.getId(), byParent));
                    return node;
                })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildChildren(Long parentId, Map<Long, List<Category>> byParent) {
        List<Category> children = byParent.get(parentId);
        if (children == null) {
            return List.of();
        }
        return children.stream()
                .map(c -> {
                    Map<String, Object> node = new java.util.LinkedHashMap<>();
                    node.put("id", c.getId());
                    node.put("name", c.getName());
                    node.put("sort", c.getSort());
                    node.put("children", buildChildren(c.getId(), byParent));
                    return node;
                })
                .collect(Collectors.toList());
    }
}
