package com.dam.damhubbackend.controller;

import com.dam.damhubbackend.common.BaseResponse;
import com.dam.damhubbackend.common.ResultUtils;
import com.dam.damhubbackend.manager.AIManager;
import com.dam.damhubbackend.model.dto.ai.AISearchRequest;
import com.dam.damhubbackend.model.dto.ai.AISearchResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * AI 素材策展接口
 */
@RestController
@RequestMapping("/ai")
public class AIController {

    @Resource
    private AIManager aiManager;

    /**
     * AI 搜图（策展助手）
     */
    @PostMapping("/search")
    public BaseResponse<AISearchResponse> search(@RequestBody AISearchRequest request) {
        return ResultUtils.success(aiManager.search(request));
    }
}
