package com.dam.damhubbackend.model.dto.ai;

import lombok.Data;

/**
 * AI 策展搜索请求
 */
@Data
public class AISearchRequest {

    /**
     * 用户搜索需求（自然语言）
     */
    private String query;

    /**
     * 返回数量
     */
    private Integer topK = 5;

    /**
     * 空间ID（可选，用于权限过滤）
     */
    private Long spaceId;

    /**
     * 分类过滤（可选）
     */
    private String category;

    /**
     * 颜色过滤（可选）
     */
    private String color;
}
