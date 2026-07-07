package com.jd.ecommerce.service;

import com.jd.ecommerce.entity.Merchant;
import com.jd.ecommerce.entity.ShopConfig;
import com.jd.ecommerce.mapper.MerchantMapper;
import com.jd.ecommerce.mapper.ShopConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ShopConfigMapper shopConfigMapper;

    public Merchant findByUserId(Long userId) {
        return merchantMapper.findByUserId(userId);
    }

    public Merchant findById(Long id) {
        return merchantMapper.findById(id);
    }

    public void updateMerchant(Merchant merchant) {
        merchantMapper.update(merchant);
    }

    public List<ShopConfig> getShopConfigs(Long merchantId) {
        return shopConfigMapper.findByMerchantId(merchantId);
    }

    public void saveShopConfig(ShopConfig config) {
        ShopConfig existing = shopConfigMapper.findByMerchantAndKey(config.getMerchantId(), config.getConfigKey());
        if (existing != null) {
            existing.setConfigValue(config.getConfigValue());
            shopConfigMapper.update(existing);
        } else {
            shopConfigMapper.insert(config);
        }
    }
}
