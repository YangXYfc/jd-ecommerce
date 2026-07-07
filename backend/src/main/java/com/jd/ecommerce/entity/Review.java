package com.jd.ecommerce.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long orderId;
    private Long productId;
    private Long userId;
    private String content;
    private Integer rating;
    private String images;
    private Integer isAnonymous;
    private String merchantReply;
    private LocalDateTime merchantReplyTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
