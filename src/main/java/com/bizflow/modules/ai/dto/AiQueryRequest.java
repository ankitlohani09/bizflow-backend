package com.bizflow.modules.ai.dto;

import com.bizflow.modules.ai.enums.AiQueryType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AiQueryRequest {
    @NotBlank(message = "Query cannot be empty")
    private String query;
    private AiQueryType type;
    private LocalDate fromDate;
    private LocalDate toDate;
}
