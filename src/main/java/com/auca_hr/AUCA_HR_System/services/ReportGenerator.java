package com.auca_hr.AUCA_HR_System.services;

import java.util.List;
import java.util.Map;

public interface ReportGenerator {
    byte[] generateReport(List<Map<String, Object>> data, String[] headers, String fileName);
    String getContentType();
    String getFileExtension();
}