package com.bizflow.modules.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiQueryResponse {
    private String query;
    private String answer;
    private String queryType;
    private Object chartData;
    private LocalDateTime createdAt;
}
