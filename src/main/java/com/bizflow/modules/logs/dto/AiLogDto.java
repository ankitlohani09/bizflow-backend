package com.bizflow.modules.logs.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiLogDto {
    private Long id;
    private Long userId;
    private String prompt;
    private String response;
    private String module;
    private Integer tokensUsed;
    private LocalDateTime createdAt;
}