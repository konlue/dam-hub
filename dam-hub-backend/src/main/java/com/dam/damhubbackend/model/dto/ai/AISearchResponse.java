package com.dam.damhubbackend.model.dto.ai;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 策展搜索响应
 */
@Data
public class AISearchResponse {

    /**
     * 意图分析结果：industry / purpose / scene / style
     */
    private Map<String, String> intent;

    /**
     * 规划的检索任务
     */
    private List<Map<String, String>> tasks;

    /**
     * 推荐素材列表
     */
    private List<Recommendation> recommendations;

    /**
     * 策展方案（LLM 生成）
     */
    private String answer;

    @Data
    public static class Recommendation {
        private Long pictureId;
        private String title;
        private String category;
        private Double score;
        private String taskType;
        private String reason;
    }
}
