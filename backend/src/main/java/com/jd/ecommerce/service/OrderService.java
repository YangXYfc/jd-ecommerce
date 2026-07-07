package com.jd.ecommerce.service;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.dto.OrderCreateRequest;
import com.jd.ecommerce.dto.ShipRequest;
import com.jd.ecommerce.entity.*;
import com.jd.ecommerce.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductSkuMapper skuMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private PaymentMapper paymentMapper;

    // Order status constants
    public static final int STATUS_PENDING_PAY = 0;
    public static final int STATUS_PENDING_SHIP = 1;
    public static final int STATUS_SHIPPED = 2;
    public static final int STATUS_RECEIVED = 3;
    public static final int STATUS_REVIEWED = 4;
    public static final int STATUS_CANCELLED = 5;

    @Transactional
    public Order createOrder(Long userId, OrderCreateRequest request) {
        Address address = addressMapper.findById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(400, "收货地址无效");
        }

        List<Cart> selectedCarts = cartMapper.findSelectedByUserId(userId);
        if (selectedCarts.isEmpty()) {
            throw new BusinessException("购物车中没有选中商品");
        }

        // Group by merchant (each merchant = one order)
        // For simplicity, assume all items from same merchant; if multiple, create multiple orders
        java.util.Map<Long, List<Cart>> byMerchant = new java.util.LinkedHashMap<>();
        for (Cart cart : selectedCarts) {
            ProductSku sku = skuMapper.findById(cart.getProductSkuId());
            if (sku == null) {
                throw new BusinessException(404, "SKU不存在: " + cart.getProductSkuId());
            }
            if (sku.getStock() < cart.getQuantity()) {
                throw new BusinessException("库存不足: " + sku.getSkuName());
            }
            Product product = productMapper.findById(sku.getProductId());
            Long merchantId = product.getMerchantId();
            byMerchant.computeIfAbsent(merchantId, k -> new ArrayList<>()).add(cart);
        }

        // For this implementation, create one order per merchant
        Order firstOrder = null;
        for (var entry : byMerchant.entrySet()) {
            Long merchantId = entry.getKey();
            List<Cart> carts = entry.getValue();

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> items = new ArrayList<>();

            for (Cart cart : carts) {
                ProductSku sku = skuMapper.findById(cart.getProductSkuId());
                Product product = productMapper.findById(sku.getProductId());

                BigDecimal subtotal = sku.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
                totalAmount = totalAmount.add(subtotal);

                OrderItem item = new OrderItem();
                item.setProductSkuId(sku.getId());
                item.setProductName(product.getName());
                item.setSkuName(sku.getSkuName());
                item.setProductImage(product.getMainImage());
                item.setQuantity(cart.getQuantity());
                item.setUnitPrice(sku.getPrice());
                item.setSubtotal(subtotal);
                items.add(item);

                // Decrease stock
                skuMapper.decreaseStock(sku.getId(), cart.getQuantity());
                // Increment sales
                productMapper.incrementSales(product.getId(), cart.getQuantity());
            }

            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setMerchantId(merchantId);
            order.setTotalAmount(totalAmount);
            order.setStatus(STATUS_PENDING_PAY);
            order.setAddressSnapshot(buildAddressSnapshot(address));
            order.setRemark(request.getRemark());
            orderMapper.insert(order);

            // Set orderId on items and batch insert
            for (OrderItem item : items) {
                item.setOrderId(order.getId());
            }
            orderItemMapper.batchInsert(items);

            if (firstOrder == null) {
                firstOrder = order;
            }
        }

        // Clear selected cart items
        cartMapper.deleteSelectedByUserId(userId);

        return firstOrder;
    }

    public Order findById(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    public Order findByOrderNo(String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    public List<Order> findByUser(Long userId, Integer status) {
        return orderMapper.findByConditions(userId, null, status);
    }

    public List<Order> findByMerchant(Long merchantId, Integer status) {
        return orderMapper.findByConditions(null, merchantId, status);
    }

    public List<Order> findAll(Integer status) {
        return orderMapper.findByConditions(null, null, status);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.findByOrderId(orderId);
    }

    @Transactional
    public void payOrder(Long orderId, Long userId) {
        Order order = findById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人订单");
        }
        if (order.getStatus() != STATUS_PENDING_PAY) {
            throw new BusinessException("订单状态不允许支付");
        }

        // Create payment record (simulated)
        Payment payment = new Payment();
        payment.setPaymentNo("PAY" + System.currentTimeMillis());
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(order.getTotalAmount());
        payment.setPayMethod("SIMULATED");
        payment.setStatus(1); // 支付成功
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        // Update order
        orderMapper.updatePayStatus(orderId, STATUS_PENDING_SHIP, LocalDateTime.now(), order.getTotalAmount());
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Order order = findById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人订单");
        }
        if (order.getStatus() != STATUS_PENDING_PAY) {
            throw new BusinessException("只能取消待支付订单");
        }

        orderMapper.updateCancelStatus(orderId, STATUS_CANCELLED, LocalDateTime.now(), reason);

        // Restore stock
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        for (OrderItem item : items) {
            skuMapper.increaseStock(item.getProductSkuId(), item.getQuantity());
        }
    }

    @Transactional
    public void shipOrder(Long orderId, Long merchantId, ShipRequest request) {
        Order order = findById(orderId);
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(403, "无权操作他人订单");
        }
        if (order.getStatus() != STATUS_PENDING_SHIP) {
            throw new BusinessException("只能发货待发货订单");
        }

        orderMapper.updateShipStatus(orderId, STATUS_SHIPPED, LocalDateTime.now(),
                request.getLogisticsCompany(), request.getLogisticsNo());
    }

    @Transactional
    public void confirmReceive(Long orderId, Long userId) {
        Order order = findById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人订单");
        }
        if (order.getStatus() != STATUS_SHIPPED) {
            throw new BusinessException("只能确认已发货订单");
        }

        orderMapper.updateReceiveStatus(orderId, STATUS_RECEIVED, LocalDateTime.now());
    }

    @Transactional
    public void markReviewed(Long orderId) {
        orderMapper.updateStatus(orderId, STATUS_REVIEWED);
    }

    // Stats
    public long countByMerchantAndStatus(Long merchantId, Integer status) {
        return orderMapper.countByMerchantAndStatus(merchantId, status);
    }

    public BigDecimal sumTotalSales() {
        return orderMapper.sumTotalSales();
    }

    public long countAll() {
        return orderMapper.countAll();
    }

    public long countToday() {
        return orderMapper.countToday();
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD" + timestamp + random;
    }

    private String buildAddressSnapshot(Address address) {
        return String.format(
                "{\"name\":\"%s\",\"phone\":\"%s\",\"province\":\"%s\",\"city\":\"%s\",\"district\":\"%s\",\"detail\":\"%s\"}",
                address.getName(), address.getPhone(), address.getProvince(),
                address.getCity(), address.getDistrict(), address.getDetail()
        );
    }
}
