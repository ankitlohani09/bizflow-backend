package com.bizflow.modules.logs.service.impl;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.logs.dto.AiLogDto;
import com.bizflow.modules.logs.entity.AiLog;
import com.bizflow.modules.logs.repository.AiLogRepository;
import com.bizflow.modules.logs.service.AiLogService;
import com.bizflow.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiLogServiceImpl implements AiLogService {

    private final AiLogRepository aiLogRepository;

    @Override
    public ApiResponse<List<AiLogDto>> getAll() {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(
                aiLogRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(this::toDto).toList());
    }

    @Override
    public ApiResponse<List<AiLogDto>> getByUser(Long userId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ApiResponse.success(aiLogRepository.findAllByTenantIdAndUserIdOrderByCreatedAtDesc(tenantId, userId)
                .stream().map(this::toDto).toList());
    }

    // Call this from AI integration service
    @Override
    public void log(String prompt, String response, String module, Integer tokensUsed) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        Long userId = SecurityUtils.getCurrentUserId();
        AiLog log = AiLog.builder().tenantId(tenantId).userId(userId).prompt(prompt).response(response).module(module)
                .tokensUsed(tokensUsed).build();
        aiLogRepository.save(log);
    }

    private AiLogDto toDto(AiLog a) {
        AiLogDto dto = new AiLogDto();
        dto.setId(a.getId());
        dto.setUserId(a.getUserId());
        dto.setPrompt(a.getPrompt());
        dto.setResponse(a.getResponse());
        dto.setModule(a.getModule());
        dto.setTokensUsed(a.getTokensUsed());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}