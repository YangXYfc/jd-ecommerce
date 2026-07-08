package com.jd.ecommerce.service;

import com.jd.ecommerce.dto.MerchantDashboardResponse;
import com.jd.ecommerce.entity.Merchant;
import com.jd.ecommerce.entity.ShopConfig;
import com.jd.ecommerce.mapper.MerchantMapper;
import com.jd.ecommerce.mapper.OrderMapper;
import com.jd.ecommerce.mapper.ProductMapper;
import com.jd.ecommerce.mapper.RefundMapper;
import com.jd.ecommerce.mapper.ShopConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ShopConfigMapper shopConfigMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RefundMapper refundMapper;

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

    /**
     * 商家维度的仪表盘统计
     */
    public MerchantDashboardResponse getMerchantDashboard(Long merchantId) {
        MerchantDashboardResponse resp = new MerchantDashboardResponse();

        // 销售额：该商家所有已支付订单（status 1~4）的总额
        BigDecimal sales = orderMapper.sumTotalSalesByMerchant(merchantId);
        resp.setTotalSales(sales != null ? sales : BigDecimal.ZERO);

        // 订单总数
        resp.setTotalOrders(orderMapper.countByMerchant(merchantId));

        // 待发货订单数
        resp.setPendingShipOrders(orderMapper.countByMerchantAndStatus(merchantId, 1));

        // 待处理退款数
        resp.setPendingRefunds(refundMapper.countByMerchantAndStatus(merchantId, 0));

        // 今日新增订单数
        resp.setTodayOrders(orderMapper.countTodayByMerchant(merchantId));

        // 商品总数
        resp.setTotalProducts(productMapper.countByMerchantId(merchantId));

        return resp;
    }
}
