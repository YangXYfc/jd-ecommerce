package com.jd.ecommerce.service;

import com.jd.ecommerce.entity.Banner;
import com.jd.ecommerce.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    public List<Banner> findAllActive() {
        return bannerMapper.findAllActive();
    }

    public List<Banner> findAll() {
        return bannerMapper.findAll();
    }

    public void addBanner(Banner banner) {
        bannerMapper.insert(banner);
    }

    public void updateBanner(Banner banner) {
        bannerMapper.update(banner);
    }

    public void deleteBanner(Long id) {
        bannerMapper.delete(id);
    }
}
