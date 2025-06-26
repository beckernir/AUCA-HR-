package com.auca_hr.AUCA_HR_System.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private String reportId;
    private String fileName;
    private String downloadUrl;
    private String status;
    private LocalDateTime generatedAt;
    private long fileSizeBytes;
    private String contentType;

    public ReportResponse(String reportId, String fileName, String downloadUrl) {
    }
}