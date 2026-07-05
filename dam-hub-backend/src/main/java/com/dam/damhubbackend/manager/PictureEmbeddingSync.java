package com.dam.damhubbackend.manager;

import com.dam.damhubbackend.model.dto.ai.AIEmbeddingRequest;
import com.dam.damhubbackend.model.entity.Picture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 图片向量同步组件（独立 Bean，确保 @Async 生效）
 */
@Slf4j
@Component
public class PictureEmbeddingSync {

    @Resource
    private AIManager aiManager;

    @Async
    public void syncEmbedding(Picture picture) {
        try {
            AIEmbeddingRequest req = new AIEmbeddingRequest();
            req.setPictureId(picture.getId());
            req.setTitle(picture.getName());
            req.setTags(picture.getTags());
            req.setCategory(picture.getCategory());
            req.setDescription(picture.getIntroduction());
            aiManager.addEmbedding(req);
        } catch (Exception e) {
            log.warn("AI 向量同步失败: pictureId={}", picture.getId(), e);
        }
    }

    @Async
    public void deleteEmbedding(Long pictureId) {
        try {
            aiManager.deleteEmbedding(pictureId);
        } catch (Exception e) {
            log.warn("AI 向量删除失败: pictureId={}", pictureId, e);
        }
    }
}
