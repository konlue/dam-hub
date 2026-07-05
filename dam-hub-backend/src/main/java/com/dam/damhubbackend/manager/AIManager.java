package com.dam.damhubbackend.manager;

import com.dam.damhubbackend.model.dto.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * AI 服务管理器 — 调用 FastAPI 后端
 */
@Slf4j
@Component
public class AIManager {

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @Resource
    private RestTemplate restTemplate;

    /**
     * 策展搜索：调用 /scout/search
     */
    public AISearchResponse search(AISearchRequest request) {
        String url = aiServiceUrl + "/scout/search";
        try {
            ResponseEntity<AISearchResponse> resp = restTemplate.postForEntity(
                    url, request, AISearchResponse.class);
            return resp.getBody();
        } catch (Exception e) {
            log.error("AI 搜图失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI 服务调用失败", e);
        }
    }

    /**
     * 图片入库 — 同步向量
     */
    public void addEmbedding(AIEmbeddingRequest request) {
        String url = aiServiceUrl + "/embedding/add";
        try {
            restTemplate.postForEntity(url, request, String.class);
            log.info("向量入库成功: pictureId={}", request.getPictureId());
        } catch (Exception e) {
            log.error("向量入库失败: pictureId={}, error={}", request.getPictureId(), e.getMessage());
        }
    }

    /**
     * 图片删除 — 删除向量
     */
    public void deleteEmbedding(Long pictureId) {
        String url = aiServiceUrl + "/embedding/" + pictureId;
        try {
            restTemplate.delete(url);
            log.info("向量删除成功: pictureId={}", pictureId);
        } catch (Exception e) {
            log.error("向量删除失败: pictureId={}, error={}", pictureId, e.getMessage());
        }
    }

    /**
     * 图片更新 — 重新生成向量
     */
    public void updateEmbedding(AIEmbeddingRequest request) {
        String url = aiServiceUrl + "/embedding/update";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AIEmbeddingRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            log.info("向量更新成功: pictureId={}", request.getPictureId());
        } catch (Exception e) {
            log.error("向量更新失败: pictureId={}, error={}", request.getPictureId(), e.getMessage());
        }
    }
}
