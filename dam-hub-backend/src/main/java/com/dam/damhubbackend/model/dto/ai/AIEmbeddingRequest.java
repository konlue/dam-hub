package com.dam.damhubbackend.model.dto.ai;

import lombok.Data;

/**
 * 向量同步请求（图片上传/更新时调用）
 */
@Data
public class AIEmbeddingRequest {

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 图片标题
     */
    private String title;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 描述（可选，由 AI 生成）
     */
    private String description;
}
