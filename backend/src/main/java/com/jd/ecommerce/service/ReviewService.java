package com.jd.ecommerce.service;

import com.jd.ecommerce.common.BusinessException;
import com.jd.ecommerce.dto.ReviewCreateRequest;
import com.jd.ecommerce.entity.Order;
import com.jd.ecommerce.entity.Review;
import com.jd.ecommerce.mapper.OrderMapper;
import com.jd.ecommerce.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    public Review findById(Long id) {
        Review review = reviewMapper.findById(id);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }
        return review;
    }

    public List<Review> findByProductId(Long productId) {
        return reviewMapper.findByProductId(productId);
    }

    public List<Review> findByOrderId(Long orderId) {
        return reviewMapper.findByOrderId(orderId);
    }

    @Transactional
    public void createReview(Long userId, ReviewCreateRequest request) {
        Order order = orderMapper.findById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作他人订单");
        }
        if (order.getStatus() != OrderService.STATUS_RECEIVED) {
            throw new BusinessException("只能评价已收货的订单");
        }

        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setProductId(request.getProductId());
        review.setUserId(userId);
        review.setContent(request.getContent());
        review.setRating(request.getRating());
        review.setImages(request.getImages());
        review.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : 0);
        reviewMapper.insert(review);

        // Mark order as reviewed
        orderService.markReviewed(order.getId());
    }

    @Transactional
    public void merchantReply(Long reviewId, Long merchantId, String reply) {
        Review review = findById(reviewId);
        // Verify the review's product belongs to this merchant via order
        Order order = orderMapper.findById(review.getOrderId());
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(403, "无权回复此评价");
        }
        reviewMapper.updateMerchantReply(reviewId, reply, LocalDateTime.now());
    }
}
