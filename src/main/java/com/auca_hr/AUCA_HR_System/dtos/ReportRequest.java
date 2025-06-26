package com.auca_hr.AUCA_HR_System.dtos;

import jakarta.persistence.SecondaryTable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    @NotNull
    @NotEmpty
    private String reportType;

    @NotNull
    @NotEmpty
    private String format;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Map<String, Object> parameters = new HashMap<>();
    private String templateName;
}